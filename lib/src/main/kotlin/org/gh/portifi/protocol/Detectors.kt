package org.gh.portifi.protocol

import org.gh.portifi.Protocol

val detectors = listOf(HttpDetector(), RespDetector(), RawProtocol())
val protocolToDetectors = detectors.associateBy(ProtocolDetector::protocol)
fun Protocol.asDetector(): ProtocolDetector {
    return protocolToDetectors[this]!!
}
