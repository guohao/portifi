package io.github.gh.portifi.channel

import io.github.gh.portifi.ProxySpec
import io.github.gh.portifi.converter.converter
import io.github.gh.portifi.protocol.detector
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class PortifiHandler(private val specs: List<ProxySpec>) : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        specs.firstOrNull {
            val detector = it.protocol().detector()
            if (detector.needMoreBytes(input)) {
                false
            } else {
                detector.accept(input)
            }
        }?.also { spec ->
            val p = ctx.pipeline()
            p.addLast(LoggingHandler(LogLevel.TRACE))
            val converter = spec.converter()
            converter.configFront(p)
            val frontHandler = FrontHandler(spec) { back -> converter.configBack(back) }
            p.addLast(frontHandler)
            p.remove(this)
        } ?: run {
            specs.firstOrNull { it.protocol().detector().needMoreBytes(input) } ?: ctx.channel().close()
        }
    }
}
