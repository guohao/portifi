package org.gh.portifi.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class BackHandler(private val inboundChannel: Channel) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        inboundChannel.flushAndCloseOnFailure(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        inboundChannel.flushAndClose()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.channel().flushAndClose()
    }
}
