package io.github.gh.portifi.converter

import io.github.gh.portifi.Protocol
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import io.netty.handler.codec.redis.RedisArrayAggregator
import io.netty.handler.codec.redis.RedisBulkStringAggregator
import io.netty.handler.codec.redis.RedisDecoder
import io.netty.handler.codec.redis.RedisEncoder
import io.netty.handler.codec.redis.RedisMessage
import io.netty.util.CharsetUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RespToHttp11Converter : Converter {

    override fun frontProtocol(): Protocol = Protocol.RESP

    override fun backProtocol(): Protocol = Protocol.HTTP1_1

    override fun configFront(p: ChannelPipeline) {
        p.addLast(RedisDecoder(false))
        p.addLast(RedisBulkStringAggregator())
        p.addLast(RedisArrayAggregator())
        p.addLast(RedisEncoder())
        p.addLast(RedisToHttp11Handler())
    }

    override fun configBack(p: ChannelPipeline) {
        p.addLast(HttpClientCodec())
        p.addLast(HttpObjectAggregator(Int.MAX_VALUE))
    }
}

private class RedisToHttp11Handler : ChannelDuplexHandler() {

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val response = (msg as FullHttpResponse).let { it.content().toString(CharsetUtil.UTF_8) }
            .let { Json.decodeFromString<Http11RespResponse>(it) }
        val redisMessage = if (response.success) {
            val buf = ctx.alloc().buffer()
            buf.writeCharSequence(response.data, CharsetUtil.UTF_8)
            FullBulkStringRedisMessage(buf)
        } else {
            ErrorRedisMessage(response.data)
        }
        ctx.write(redisMessage, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val request = (msg as RedisMessage).toHttpRequest()
        ctx.fireChannelRead(request)
    }
}

private sealed interface RespToHttpRequestMapper {
    fun accept(redisMessage: RedisMessage): Boolean
    fun map(redisMessage: RedisMessage): HttpRequest
}

private fun RedisMessage.toHttpRequest(): HttpRequest {
    return requestMappers.first { it.accept(this) }.map(this)
}

private val requestMappers = listOf(GetMapper, SetMapper)

private object SetMapper : RespToHttpRequestMapper {
    private const val SET_PARAM_SIZE = 3

    override fun accept(redisMessage: RedisMessage) =
        redisMessage.takeIf { it is ArrayRedisMessage }?.let { it as ArrayRedisMessage }
            ?.takeIf { it.children().size == SET_PARAM_SIZE }?.takeIf { it.children()[0] is FullBulkStringRedisMessage }
            ?.let { it.children()[0] as FullBulkStringRedisMessage }
            ?.takeIf { it.textContent().contentEquals("set", true) }?.let { true } ?: false

    override fun map(redisMessage: RedisMessage): HttpRequest {
        val children = (redisMessage as ArrayRedisMessage).children()
        val key = (children[1] as FullBulkStringRedisMessage).textContent()
        val value = (children[2] as FullBulkStringRedisMessage).content().retain()
        val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/set/$key", value)
        request.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain")
            .add(HttpHeaderNames.CONTENT_LENGTH, value.readableBytes())
        return request
    }
}

private object GetMapper : RespToHttpRequestMapper {
    private const val GET_PARAM_SIZE = 2

    override fun accept(redisMessage: RedisMessage) =
        redisMessage.takeIf { it is ArrayRedisMessage }?.let { it as ArrayRedisMessage }
            ?.takeIf { it.children().size == GET_PARAM_SIZE }?.takeIf { it.children()[0] is FullBulkStringRedisMessage }
            ?.let { it.children()[0] as FullBulkStringRedisMessage }
            ?.takeIf { it.textContent().contentEquals("get", true) }?.let { true } ?: false

    override fun map(redisMessage: RedisMessage): HttpRequest {
        val children = (redisMessage as ArrayRedisMessage).children()
        val key = (children[1] as FullBulkStringRedisMessage).textContent()
        return DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/get/$key")
    }
}

@Serializable
data class Http11RespResponse(
    val command: String,
    val success: Boolean,
    val data: String
)

fun FullBulkStringRedisMessage.textContent(): String {
    return content().toString(CharsetUtil.UTF_8)
}
