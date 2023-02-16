package io.github.guohao.portifi

import io.github.guohao.portifi.channel.PortifiHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultThreadFactory
import java.util.concurrent.Semaphore

class Portifi(private val specs: List<ProxySpec>) {
    private val stop = Semaphore(1)
    private val stopActions = mutableListOf<Runnable>()
    fun start(port: Int): Portifi = start("0.0.0.0", port)
    fun start(host: String, port: Int): Portifi {
        if (!stop.tryAcquire()) {
            return this
        }
        val boss = NioEventLoopGroup(1, DefaultThreadFactory("portifi-boos"))
        val worker = NioEventLoopGroup(DefaultThreadFactory("portifi-worker"))
        val bootstrap = ServerBootstrap()
            .group(boss, worker)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(
                object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline()
                            .addLast(PortifiHandler(specs))
                    }
                },
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
