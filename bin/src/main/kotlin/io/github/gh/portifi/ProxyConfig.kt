package io.github.gh.portifi

import kotlinx.serialization.Serializable

@Serializable
data class ProxyConfig(
    val port: Int = 9999,
    val host: String = "0.0.0.0",
    val specs: List<Spec>
)

@Serializable
data class Spec(
    val protocol: String,
    val port: Int,
    val host: String = "localhost"
)

fun Spec.toLibSpec(): ProxySpec =
    ProxySpecBuilder(port)
        .protocol(Protocol.valueOf(protocol))
        .host(host)
        .build()
