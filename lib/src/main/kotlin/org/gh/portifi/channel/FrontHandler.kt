package org.gh.portifi.channel

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelPromise
import org.gh.portifi.ProxySpec

class FrontHandler(private val spec: ProxySpec) : ChannelInboundHandlerAdapter() {
    private lateinit var connectPromise: ChannelPromise

    private lateinit var outboundChannel: Channel

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val inboundChannel = ctx.channel()
        val b = Bootstrap()
        b.group(inboundChannel.eventLoop())
            .channel(ctx.channel().javaClass)
            .handler(BackHandler(inboundChannel))
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
            }
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

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.channel().flushAndClose()
    }
}
