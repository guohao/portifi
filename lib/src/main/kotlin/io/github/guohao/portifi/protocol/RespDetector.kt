package io.github.guohao.portifi.protocol

import io.github.guohao.portifi.Protocol
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.redis.RedisMessageType

class RespDetector : ProtocolDetector {
    override fun protocol(): Protocol = Protocol.RESP

    override fun accept(input: ByteBuf): Boolean {
        input.markReaderIndex()
        val readFrom = RedisMessageType.readFrom(input, true)
        input.resetReaderIndex()
        return readFrom != RedisMessageType.INLINE_COMMAND
    }
}
