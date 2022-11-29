package io.github.gh.portifi.protocol

import io.github.gh.portifi.Protocol
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.http2.Http2CodecUtil
import kotlin.math.min

class Http2Detector : ProtocolDetector {
    private val clientPreface = Http2CodecUtil.connectionPrefaceBuf()

    override fun protocol(): Protocol = Protocol.HTTP2

    override fun needMoreBytes(input: ByteBuf): Boolean {
        val prefaceLen: Int = clientPreface.readableBytes()
        val bytesRead = min(input.readableBytes(), prefaceLen)

        if (bytesRead == 0) {
            return true
        }

        return ByteBufUtil.equals(
            input,
            0,
            clientPreface,
            0,
            bytesRead
        ) && bytesRead < prefaceLen
    }

    override fun accept(input: ByteBuf): Boolean {
        val prefaceLen: Int = clientPreface.readableBytes()
        val bytesRead = min(input.readableBytes(), prefaceLen)

        return ByteBufUtil.equals(
            input,
            0,
            clientPreface,
            0,
            bytesRead
        )
    }
}
