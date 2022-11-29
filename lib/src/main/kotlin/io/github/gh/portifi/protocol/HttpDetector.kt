package io.github.gh.portifi.protocol

import io.github.gh.portifi.Protocol
import io.netty.buffer.ByteBuf

const val MIN_BYTES_TO_DETECT = 2

class HttpDetector : ProtocolDetector {
    override fun protocol(): Protocol = Protocol.HTTP1_1

    override fun needMoreBytes(input: ByteBuf): Boolean = input.readableBytes() < MIN_BYTES_TO_DETECT

    override fun accept(input: ByteBuf): Boolean {
        val magic1 = input.getUnsignedByte(input.readerIndex()).toInt().toChar()
        val magic2 = input.getUnsignedByte(input.readerIndex() + 1).toInt().toChar()
        return when (magic1) {
            'G' ->
                magic2 == 'E'

            'P' ->
                magic2 == 'O' || magic2 == 'U' || magic2 == 'A'

            'H' ->
                magic2 == 'E'

            'D' ->
                magic2 == 'E'

            'O' ->
                magic2 == 'P'

            'T' ->
                magic2 == 'R'

            'C' ->
                magic2 == 'O'

            else ->
                false
        }
    }
}
