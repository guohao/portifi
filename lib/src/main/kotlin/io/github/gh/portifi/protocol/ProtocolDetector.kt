package io.github.gh.portifi.protocol

import io.github.gh.portifi.Protocol
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext

interface ProtocolDetector {

    fun protocol(): Protocol

    fun needMoreBytes(input: ByteBuf): Boolean = false

    fun accept(input: ByteBuf): Boolean

    fun configure(ctx: ChannelHandlerContext) = run {}
}
