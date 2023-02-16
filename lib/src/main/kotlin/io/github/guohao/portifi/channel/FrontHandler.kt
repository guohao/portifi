package io.github.guohao.portifi.channel

import io.github.guohao.portifi.ProxySpec
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.ChannelPromise
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.LoggerFactory
import java.io.IOException

private val log = LoggerFactory.getLogger(FrontHandler::class.java)
class FrontHandler(
    private val spec: ProxySpec,
    private val backConfig: (ChannelPipeline) -> Unit = {},
) : ChannelInboundHandlerAdapter() {
    private lateinit var connectPromise: ChannelPromise

    private lateinit var outboundChannel: Channel

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val inboundChannel = ctx.channel()
        val b = Bootstrap()
            .group(inboundChannel.eventLoop())
            .channel(ctx.channel().javaClass)
            .handler(
                object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        val p = ch.pipeline()
                        p.addLast(LoggingHandler(LogLevel.TRACE))
                        backConfig(p)
                        p.addLast(BackHandler(inboundChannel))
                    }
                },
            )
        val f: ChannelFuture = b.connect(spec.host(), spec.port())
        this.outboundChannel = f.channel()
        this.connectPromise = ctx.newPromise()
        f.addListener(
            ChannelFutureListener { future ->
                if (future.isSuccess) {
                    connectPromise.trySuccess()
                } else {
                    connectPromise.tryFailure(future.cause())
                }
            },
        )
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (outboundChannel.isActive) {
            writeToBack(msg)
        } else {
            connectPromise.addListener {
                if (it.isSuccess) {
                    writeToBack(msg)
                } else {
                    ctx.channel().close()
                }
            }
        }
    }

    private fun writeToBack(msg: Any) {
        outboundChannel.flushAndCloseOnFailure(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        outboundChannel.flushAndClose()
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is IOException) {
            // ignored
        } else {
            log.error("[portifi] Caught exception with spec=$spec channel=${ctx.channel()}", cause)
        }

        ctx.channel().flushAndClose()
    }
}
