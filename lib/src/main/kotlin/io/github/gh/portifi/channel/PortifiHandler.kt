package io.github.gh.portifi.channel

import io.github.gh.portifi.ProxySpec
import io.github.gh.portifi.protocol.asDetector
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

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
