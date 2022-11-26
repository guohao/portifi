package org.gh.portifi

interface ProxySpec {
    fun port(): Int
}

class ProxySpecBuilder(private val port: Int) {
    fun build():ProxySpec = SimpleProxySpec(port)
}

class SimpleProxySpec(private val port: Int) : ProxySpec {
    override fun port(): Int = port
}