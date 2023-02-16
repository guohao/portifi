package io.github.guohao.portifi.channel

import io.github.guohao.portifi.ProxySpec
import io.github.guohao.portifi.converter.converter
import io.github.guohao.portifi.protocol.detector
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.LoggerFactory
import java.io.IOException

private val log = LoggerFactory.getLogger(PortifiHandler::class.java)

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

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is IOException) {
            log.debug("[portifi] IOException before recognize protocol. channel=${ctx.channel()}", cause)
        } else {
            log.warn("[portifi] Exception before recognize protocol. channel=${ctx.channel()}", cause)
        }
    }
}
