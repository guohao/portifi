package io.github.guohao.portifi

private const val HTTP_BACKEND_PORT = 8088
private const val GRPC_BACKEND_PORT = 50051
private const val FRONTEND_PORT = 8888

fun main() {
    val server = listOf(
        ProxySpecBuilder(GRPC_BACKEND_PORT)
            .protocol(Protocol.HTTP2)
            .build(),
        ProxySpecBuilder(HTTP_BACKEND_PORT)
            .protocol(Protocol.RESP)
            .convertTo(Protocol.HTTP1_1)
            .build()
    ).asServer()
        .start(FRONTEND_PORT)
    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop()
        }
    )
    server.blockUntilStop()
}
