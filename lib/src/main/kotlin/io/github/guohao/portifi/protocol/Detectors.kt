package io.github.guohao.portifi.protocol

import io.github.guohao.portifi.Protocol

val detectors = listOf(HttpDetector(), Http2Detector(), RespDetector(), RawProtocol())
val protocolToDetectors = detectors.associateBy(ProtocolDetector::protocol)
fun Protocol.detector(): ProtocolDetector {
    return protocolToDetectors[this]!!
}
