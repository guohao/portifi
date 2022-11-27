package org.gh.portifi.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import org.gh.portifi.Protocol

interface ProtocolDetector {

    fun protocol(): Protocol

    fun needMoreBytes(input: ByteBuf): Boolean = false

    fun accept(input: ByteBuf): Boolean

    fun configure(ctx: ChannelHandlerContext) = run {}
}
