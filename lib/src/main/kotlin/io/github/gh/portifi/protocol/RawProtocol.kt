package io.github.gh.portifi.protocol

import io.github.gh.portifi.Protocol
import io.netty.buffer.ByteBuf

class RawProtocol : ProtocolDetector {
    override fun protocol(): Protocol = Protocol.RAW
    override fun accept(input: ByteBuf): Boolean = true
}
