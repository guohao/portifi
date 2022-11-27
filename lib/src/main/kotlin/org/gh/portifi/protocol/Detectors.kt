package org.gh.portifi.protocol

import org.gh.portifi.Protocol


val detectors = listOf(HttpDetector(), RespDetector())
fun Protocol.asDetector(): ProtocolDetector {
    return detectors.first { it.protocol() == this }
}
