package io.github.guohao.portifi

enum class Protocol {
    UNSET,
    RAW,
    HTTP1_1,
    HTTP2,
    RESP,
    SOCKS5,
    MQTT,
}

interface ProxySpec {
    fun port(): Int
    fun protocol(): Protocol = Protocol.RAW
    fun host(): String = "localhost"
    fun convertTo(): Protocol = Protocol.UNSET
    fun enableTls(): Boolean = false
}

class ProxySpecBuilder(
    private val port: Int,
    private val protocol: Protocol = Protocol.RAW,
    private val host: String = "localhost",
    private val convertTo: Protocol = protocol,
    private val enableTls: Boolean = false,
) {
    fun protocol(protocol: Protocol): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, convertTo, enableTls)
    fun host(host: String): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, convertTo, enableTls)

    fun enableTls(): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, convertTo, true)

    fun convertTo(backend: Protocol): ProxySpecBuilder = ProxySpecBuilder(port, protocol, host, backend, enableTls)
    fun build(): ProxySpec = SimpleProxySpec(port, protocol, host, convertTo, enableTls)
}

class SimpleProxySpec(
    private val port: Int,
    private val protocol: Protocol,
    private val host: String,
    private val convertTo: Protocol,
    private val enableTls: Boolean,
) : ProxySpec {
    override fun port(): Int = port
    override fun protocol(): Protocol = protocol
    override fun enableTls(): Boolean = enableTls
    override fun host(): String = host

    override fun convertTo(): Protocol = convertTo

    override fun toString(): String {
        return "port=$port front_protocol=$protocol back_protocol=$convertTo host=$host enable_tls=$enableTls"
    }
}
