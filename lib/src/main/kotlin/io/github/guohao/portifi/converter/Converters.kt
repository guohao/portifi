package io.github.guohao.portifi.converter

import io.github.guohao.portifi.Protocol
import io.github.guohao.portifi.ProxySpec
import io.netty.channel.ChannelPipeline

interface Converter {
    fun frontProtocol(): Protocol

    fun backProtocol(): Protocol

    fun configFront(p: ChannelPipeline) = Unit

    fun configBack(p: ChannelPipeline) = Unit
}

val converters = listOf(NothingConverter, RespToHttp11Converter())

object NothingConverter : Converter {
    override fun frontProtocol(): Protocol = Protocol.RAW

    override fun backProtocol(): Protocol = Protocol.RAW
}

fun ProxySpec.converter(): Converter {
    return if (convertTo() == Protocol.UNSET || protocol() == convertTo()) {
        NothingConverter
    } else {
        converters.first { protocol() == it.frontProtocol() && convertTo() == it.backProtocol() }
    }
}
