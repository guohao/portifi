package org.gh.portifi

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOption

class FrontHandler(private val port: Int) : ChannelInboundHandlerAdapter() {

    private var outboundChannel: Channel? = null

    override fun channelActive(ctx: ChannelHandlerContext) {
        val inboundChannel = ctx.channel()
        val b = Bootstrap()
        b.group(inboundChannel.eventLoop())
            .channel(ctx.channel().javaClass)
            .option(ChannelOption.AUTO_READ, false)
            .handler(BackHandler(inboundChannel))
        val f: ChannelFuture = b.connect("localhost", port)
        this.outboundChannel = f.channel()
        f.addListener(
            ChannelFutureListener { future ->
                if (future.isSuccess) {
                    inboundChannel.read()
                } else {
                    inboundChannel.close()
                }
            }
        )
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        outboundChannel ?: return
        if (outboundChannel!!.isActive) {
            outboundChannel!!.writeAndFlush(msg)
                .addListener(ChannelFutureListener {
                    if (it.isSuccess) {
                        ctx.channel().read()
                    } else {
                        it.channel().close()
                    }
                })
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        outboundChannel?.writeAndFlush(Unpooled.EMPTY_BUFFER)
            ?.addListener(ChannelFutureListener.CLOSE)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.channel()
            .writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener(ChannelFutureListener.CLOSE)
    }
}
