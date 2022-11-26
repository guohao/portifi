package org.gh.portifi

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.Semaphore

class Portfifi(private val specs: List<ProxySpec>) {
    private val stop = Semaphore(1)
    private val stopActions = mutableListOf<Runnable>()
    fun start(port: Int): Portfifi {
        if (!stop.tryAcquire()) {
            return this
        }
        val boss = NioEventLoopGroup(1)
        val worker = NioEventLoopGroup()
        val bootstrap = ServerBootstrap()
            .group(boss, worker)
            .channel(NioServerSocketChannel::class.java)
            .childOption(ChannelOption.AUTO_READ, false)
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

fun List<ProxySpec>.asServer(): Portfifi {
    return Portfifi(this)
}

fun ProxySpec.asServer(): Portfifi {
    return listOf(this).asServer()
}

fun main() {
    val server = ProxySpecBuilder(8080)
        .build()
        .asServer()
        .start(9999)
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop()
    })
    server.blockUntilStop()
}
