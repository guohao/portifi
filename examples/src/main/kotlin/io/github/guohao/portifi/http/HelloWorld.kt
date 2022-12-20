package io.github.guohao.portifi.http

import io.github.guohao.portifi.Portifi
import io.github.guohao.portifi.Protocol
import io.github.guohao.portifi.ProxySpecBuilder
import io.github.guohao.portifi.asServer
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.slf4j.LoggerFactory

private const val BACK_PORT = 8080
private const val FRONT_PORT = 9999

private val log = LoggerFactory.getLogger(HelloWorld::class.java)

class HelloWorld {
    private lateinit var backend: Http4kServer
    private lateinit var front: Portifi

    fun start() {
        val app = { request: Request -> Response(OK).body("Hello, ${request.query("name")}!") }
        this.backend = app.asServer(Undertow(BACK_PORT)).start()
        this.front = ProxySpecBuilder(BACK_PORT)
            .protocol(Protocol.HTTP1_1)
            .build()
            .asServer()
        front.start(FRONT_PORT)
    }

    fun stop() {
        front.stop()
        backend.stop()
    }
}

fun main() {
    val helloWorld = HelloWorld()
    helloWorld.start()
    val request = Request(Method.GET, "http://localhost:$FRONT_PORT?name=portifi")

    val client: HttpHandler = JavaHttpClient()

    log.info("${client(request)}")
    helloWorld.stop()
}
