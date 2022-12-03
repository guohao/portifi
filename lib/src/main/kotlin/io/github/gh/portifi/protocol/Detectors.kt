package io.github.gh.portifi.protocol

import io.github.gh.portifi.Protocol

val detectors = listOf(HttpDetector(), Http2Detector(), RespDetector(), RawProtocol())
val protocolToDetectors = detectors.associateBy(ProtocolDetector::protocol)
fun Protocol.detector(): ProtocolDetector {
    return protocolToDetectors[this]!!
}
