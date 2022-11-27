package org.gh.portifi.protocol

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.redis.RedisMessageType
import org.gh.portifi.Protocol

class RespDetector : ProtocolDetector {
    override fun protocol(): Protocol = Protocol.RESP

    override fun accept(input: ByteBuf): Boolean {
        input.markReaderIndex()
        val readFrom = RedisMessageType.readFrom(input, true)
        input.resetReaderIndex()
        return readFrom != RedisMessageType.INLINE_COMMAND
    }
}
