package io.github.guohao.portifi.redis

import io.github.guohao.portifi.Portifi
import io.github.guohao.portifi.Protocol
import io.github.guohao.portifi.ProxySpecBuilder
import io.github.guohao.portifi.asServer
import org.redisson.Redisson
import org.redisson.api.redisnode.RedisNodes
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import redis.embedded.RedisServer

private const val BACK_PORT = 6379
private const val FRONT_PORT = 9999

private val log = LoggerFactory.getLogger(PingPong::class.java)

class PingPong {
    private lateinit var backend: RedisServer
    private lateinit var front: Portifi

    fun start() {
        this.backend = RedisServer(BACK_PORT)
        this.backend.start()
        this.front = ProxySpecBuilder(BACK_PORT)
            .protocol(Protocol.RESP)
            .build()
            .asServer()
            .start(FRONT_PORT)
    }

    fun stop() {
        this.front.stop()
        this.backend.stop()
    }
}

fun main() {
    val pingPong = PingPong()
    pingPong.start()
    val config = Config()
        .apply {
            useSingleServer().address = "redis://localhost:$FRONT_PORT"
        }
    val client = Redisson.create(config)
    val ret = client.getRedisNodes(RedisNodes.SINGLE).instance.ping()
    log.info("Ping: $ret")
    pingPong.stop()
}
