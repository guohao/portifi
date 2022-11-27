package org.gh.portifi.protocol

import org.gh.portifi.Protocol

val detectors = listOf(HttpDetector(), Http2Detector(), RespDetector(), RawProtocol())
val protocolToDetectors = detectors.associateBy(ProtocolDetector::protocol)
fun Protocol.asDetector(): ProtocolDetector {
    return protocolToDetectors[this]!!
}
