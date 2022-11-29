package org.gh.portifi.channel

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.gh.portifi.ProxySpec
import org.gh.portifi.protocol.asDetector

class PortifiHandler(private val specs: List<ProxySpec>) : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        specs.firstOrNull {
            val detector = it.protocol().asDetector()
            if (detector.needMoreBytes(input)) {
                false
            } else {
                detector.accept(input)
            }
        }?.also {
            ctx.pipeline()
                .addLast(FrontHandler(it))
                .remove(this)
        } ?: run {
            specs.firstOrNull { it.protocol().asDetector().needMoreBytes(input) }
                ?: ctx.channel().close()
        }
    }
}
