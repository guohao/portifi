package io.github.gh.portifi.converter

import io.github.gh.portifi.Protocol
import io.github.gh.portifi.ProxySpec
import io.netty.channel.ChannelPipeline

interface Converter {
    fun frontProtocol(): Protocol

    fun backProtocol(): Protocol

    fun configFront(p: ChannelPipeline) = Unit

    fun configBack(p: ChannelPipeline) = Unit
}

val converters = listOf(NothingConverter(), RespToHttp11Converter())

class NothingConverter : Converter {
    override fun frontProtocol(): Protocol = Protocol.RAW

    override fun backProtocol(): Protocol = Protocol.RAW
}

fun ProxySpec.converter(): Converter {
    return converters.first { protocol() == it.frontProtocol() && convertTo() == it.backProtocol() }
}
