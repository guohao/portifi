package io.github.guohao.portifi

import com.charleskorn.kaml.Yaml

const val CONFIG_FILE = "portifi.yml"

class Main {
    fun start() {
        val yaml = this::class.java.classLoader.getResource(CONFIG_FILE)!!.readText()
        val config = Yaml.default.decodeFromString(ProxyConfig.serializer(), yaml)
        val specs = config.specs.map(Spec::toLibSpec)
        val server = Portifi(specs).start(config.host, config.port)
        Runtime.getRuntime().addShutdownHook(
            Thread {
                server.stop()
            }
        )
        server.blockUntilStop()
    }
}

fun main() {
    Main().start()
}
