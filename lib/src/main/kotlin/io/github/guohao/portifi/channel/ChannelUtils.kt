package io.github.guohao.portifi.channel

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener

fun Channel.flushAndCloseOnFailure(msg: Any) {
    writeAndFlush(msg)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
}

fun Channel.flushAndClose(msg: Any) {
    writeAndFlush(msg)
        .addListener(ChannelFutureListener.CLOSE)
}

fun Channel.flushAndClose() {
    flushAndClose(Unpooled.EMPTY_BUFFER)
}
