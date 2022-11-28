package org.gh.portifi.grpc

import HelloWorldServer
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.Server
import org.gh.portifi.Portifi
import org.gh.portifi.Protocol
import org.gh.portifi.ProxySpecBuilder
import org.gh.portifi.asServer

private const val BACK_PORT = 50051
private const val FRONT_PORT = 9999

class Greeter {
    private lateinit var backend: Server
    private lateinit var front: Portifi

    fun start() {
        this.backend = HelloWorldServer(BACK_PORT).server
        this.front = ProxySpecBuilder(BACK_PORT)
            .protocol(Protocol.HTTP2)
            .build()
            .asServer()
        backend.start()
        front.start(FRONT_PORT)
    }

    fun stop() {
        front.stop()
        backend.shutdown()
    }
}

fun main() {
    val greeter = Greeter()
    greeter.start()

    val channel = Grpc.newChannelBuilder("localhost:$FRONT_PORT", InsecureChannelCredentials.create())
        .build()
    val client = HelloWorldClient(channel)
    client.greet("portifi")

    greeter.stop()
}
