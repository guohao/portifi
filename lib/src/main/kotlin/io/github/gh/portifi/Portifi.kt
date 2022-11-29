package io.github.gh.portifi

import io.github.gh.portifi.channel.PortifiHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.Semaphore

const val HTTP_BACKEND_PORT = 8080
const val RESP_BACKEND_PORT = 6379
const val GRPC_BACKEND_PORT = 50051
const val FRONTEND_PORT = 9999

class Portifi(private val specs: List<ProxySpec>) {
    private val stop = Semaphore(1)
    private val stopActions = mutableListOf<Runnable>()
    fun start(port: Int): Portifi = start("0.0.0.0", port)
    fun start(host: String, port: Int): Portifi {
        if (!stop.tryAcquire()) {
            return this
        }
        val boss = NioEventLoopGroup(1)
        val worker = NioEventLoopGroup()
        val bootstrap = ServerBootstrap()
            .group(boss, worker)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(
                object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline()
                            .addLast(PortifiHandler(specs))
                    }
                }
            )
            .bind(host, port)
        stopActions.add {
            bootstrap.channel().close()
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
        ProxySpecBuilder(GRPC_BACKEND_PORT)
            .protocol(Protocol.HTTP2)
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
