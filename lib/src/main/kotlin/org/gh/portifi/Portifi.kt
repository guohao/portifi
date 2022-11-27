package org.gh.portifi

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.Semaphore

const val HTTP_BACKEND_PORT = 8080
const val RESP_BACKEND_PORT = 6379
const val FRONTEND_PORT = 9999

class Portifi(private val specs: List<ProxySpec>) {
    private val stop = Semaphore(1)
    private val stopActions = mutableListOf<Runnable>()
    fun start(port: Int): Portifi {
        if (!stop.tryAcquire()) {
            return this
        }
        val boss = NioEventLoopGroup(1)
        val worker = NioEventLoopGroup()
        val bootstrap = ServerBootstrap()
            .group(boss, worker)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(InboundHandlerInitializer(specs))
            .bind(port)
        stopActions.add {
            bootstrap.channel().closeFuture().sync()
            boss.shutdownGracefully()
            worker.shutdownGracefully()
        }
        bootstrap.sync()
        return this
    }

    fun stop() {
        stopActions.forEach(Runnable::run)
        stop.release()
    }

    fun blockUntilStop() {
        stop.acquire()
    }
}

fun List<ProxySpec>.asServer(): Portifi {
    return Portifi(this)
}

fun ProxySpec.asServer(): Portifi {
    return listOf(this).asServer()
}

fun main() {
    val server = listOf(
        ProxySpecBuilder(HTTP_BACKEND_PORT)
            .protocol(Protocol.HTTP1_1)
            .build(),
        ProxySpecBuilder(RESP_BACKEND_PORT)
            .protocol(Protocol.RESP)
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
