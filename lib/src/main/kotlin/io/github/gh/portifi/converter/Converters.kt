package io.github.gh.portifi.converter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.gh.portifi.Protocol
import io.github.gh.portifi.ProxySpec
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.CodecException
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import io.netty.handler.codec.redis.IntegerRedisMessage
import io.netty.handler.codec.redis.RedisArrayAggregator
import io.netty.handler.codec.redis.RedisBulkStringAggregator
import io.netty.handler.codec.redis.RedisDecoder
import io.netty.handler.codec.redis.RedisEncoder
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.RedisMessageType
import io.netty.handler.codec.redis.SimpleStringRedisMessage
import io.netty.util.CharsetUtil

private val mapper = ObjectMapper()

interface Converter {
    fun frontProtocol(): Protocol

    fun backProtocol(): Protocol

    fun configFront(p: ChannelPipeline) = Unit

    fun configBack(p: ChannelPipeline) = Unit
}

val converters = listOf(NothingConverter(), RespToHttp11Converter())

class NothingConverter : Converter {
    override fun frontProtocol(): Protocol = Protocol.RAW

    override fun backProtocol(): Protocol = Protocol.RAW
}

class RespToHttp11Converter : Converter {
    override fun frontProtocol(): Protocol = Protocol.RESP

    override fun backProtocol(): Protocol = Protocol.HTTP1_1

    override fun configFront(p: ChannelPipeline) {
        p.addLast(RedisDecoder())
        p.addLast(RedisBulkStringAggregator())
        p.addLast(RedisArrayAggregator())
        p.addLast(RedisEncoder())
        p.addLast(RedisToHttp11Handler())
    }

    override fun configBack(p: ChannelPipeline) {
        p.addLast(HttpClientCodec())
        p.addLast(HttpObjectAggregator(Int.MAX_VALUE))
    }

    class RedisToHttp11Handler : ChannelDuplexHandler() {

        override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
            val response = msg as FullHttpResponse
            val params = mapper.readValue(response.content().toString(CharsetUtil.UTF_8), List::class.java)
            val redisMessage = pojoToRedis(ctx.alloc(), params)
            ctx.write(redisMessage, promise)
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            val params = redisToPojo(msg as RedisMessage)
            val json = mapper.writeValueAsBytes(params)
            val payload = ctx.alloc().buffer(json.size, json.size).writeBytes(json)
            val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/redis", payload)
            request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json")
                .add(HttpHeaderNames.CONTENT_LENGTH, payload.readableBytes())
            ctx.fireChannelRead(request)
        }

        private fun pojoToRedis(alloc: ByteBufAllocator, msg: List<*>): RedisMessage = when (msg[0]) {
            RedisMessageType.SIMPLE_STRING.name -> {
                SimpleStringRedisMessage(msg[1] as String)
            }

            RedisMessageType.ERROR.name -> {
                ErrorRedisMessage(msg[1] as String)
            }

            RedisMessageType.INTEGER.name -> {
                IntegerRedisMessage(msg[1] as Long)
            }

            RedisMessageType.BULK_STRING.name -> {
                val buf = alloc.buffer()
                buf.writeCharSequence(msg[1] as String, CharsetUtil.UTF_8)
                FullBulkStringRedisMessage(buf)
            }

            RedisMessageType.ARRAY_HEADER.name -> {
                val child = (msg[1] as List<*>).map { pojoToRedis(alloc, it as List<*>) }
                ArrayRedisMessage(child)
            }

            else -> {
                throw CodecException("unknown message type: $msg")
            }
        }

        private fun redisToPojo(msg: RedisMessage): List<Any> = when (msg) {
            is SimpleStringRedisMessage -> {
                listOf(RedisMessageType.SIMPLE_STRING.name, msg.content())
            }

            is ErrorRedisMessage -> {
                listOf(RedisMessageType.ERROR.name, msg.content())
            }

            is IntegerRedisMessage -> {
                listOf(RedisMessageType.INTEGER.name, msg.value())
            }

            is FullBulkStringRedisMessage -> {
                listOf(RedisMessageType.BULK_STRING.name, msg.content().toString(CharsetUtil.UTF_8))
            }

            is ArrayRedisMessage -> {
                listOf(
                    RedisMessageType.ARRAY_HEADER.name,
                    msg.children().map(::redisToPojo)
                )
            }

            else -> {
                throw CodecException("unknown message type: $msg")
            }
        }
    }
}

fun ProxySpec.converter(): Converter {
    return converters.first { protocol() == it.frontProtocol() && convertTo() == it.backProtocol() }
}
