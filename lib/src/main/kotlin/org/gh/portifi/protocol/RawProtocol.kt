package org.gh.portifi.protocol

import io.netty.buffer.ByteBuf
import org.gh.portifi.Protocol

class RawProtocol : ProtocolDetector {
    override fun protocol(): Protocol = Protocol.RAW
    override fun accept(input: ByteBuf): Boolean = true
}
