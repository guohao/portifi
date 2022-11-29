package io.github.gh.portifi

enum class Protocol {
    RAW,
    HTTP1_1,
    HTTP2,
    RESP,
    SOCKS5,
    MQTT
}

interface ProxySpec {
    fun port(): Int
    fun protocol(): Protocol = Protocol.RAW
    fun host(): String = "localhost"
    fun enableTls(): Boolean = false
}

class ProxySpecBuilder(
    private val port: Int,
    private val protocol: Protocol = Protocol.RAW,
    private val host: String = "localhost",
    private val enableTls: Boolean = false
) {
    fun protocol(protocol: Protocol): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, enableTls)
    fun host(host: String): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, enableTls)

    fun enableTls(): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, true)

    fun build(): ProxySpec = SimpleProxySpec(port, protocol, host, enableTls)
}

class SimpleProxySpec(
    private val port: Int,
    private val protocol: Protocol,
    private val host: String,
    private val enableTls: Boolean
) : ProxySpec {
    override fun port(): Int = port
    override fun protocol(): Protocol = protocol
    override fun enableTls(): Boolean = enableTls
    override fun host(): String = host
}
