package io.github.gh.portifi.converter

import io.github.gh.portifi.Protocol
import io.netty.buffer.Unpooled
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
import io.netty.handler.codec.redis.IntegerRedisMessage
import io.netty.handler.codec.redis.RedisArrayAggregator
import io.netty.handler.codec.redis.RedisBulkStringAggregator
import io.netty.handler.codec.redis.RedisDecoder
import io.netty.handler.codec.redis.RedisEncoder
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage
import io.netty.util.CharsetUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.long

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
        val response = (msg as FullHttpResponse).content()
            .toString(CharsetUtil.UTF_8)
            .let { Json.decodeFromString<RespResponseBox<JsonElement>>(it) }
        val redisMessage = if (response.success) {
            response.data.toRedisMessage()
        } else {
            ErrorRedisMessage((response.data as JsonPrimitive).content)
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

private val requestMappers =
    listOf(GetMapper, SetMapper, ExpireMapper, InfoMapper, PingMapper, CommandDocsMapper, CommandMapper, DelMapper)

private object SetMapper : RespToHttpRequestMapper {

    private const val SET_NUM = 3
    override fun accept(redisMessage: RedisMessage) = "set".equals(redisMessage.firstString(SET_NUM), true)

    override fun map(redisMessage: RedisMessage): HttpRequest {
        val children = (redisMessage as ArrayRedisMessage).children()
        val key = (children[1] as FullBulkStringRedisMessage).textContent()
        val value = (children[2] as FullBulkStringRedisMessage).content().retain()
        val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/set/$key", value)
        request.headers()
            .add(HttpHeaderNames.CONTENT_TYPE, "text/plain")
            .add(HttpHeaderNames.CONTENT_LENGTH, value.readableBytes())
        return request
    }
}

abstract class SingleCommand(private val command: String) : RespToHttpRequestMapper {
    override fun accept(redisMessage: RedisMessage): Boolean = redisMessage.isSingleCommand(command)

    override fun map(redisMessage: RedisMessage): HttpRequest = command.httpRequest()
}

abstract class QueryCommand(private vararg val commands: String) : RespToHttpRequestMapper {
    override fun accept(redisMessage: RedisMessage): Boolean = commands.zip(redisMessage.asArray()!!.children())
        .all { it.first.equals(it.second.textContent(), true) }

    override fun map(redisMessage: RedisMessage): HttpRequest = redisMessage.httpRequest()

    private fun RedisMessage.httpRequest(): HttpRequest {
        var uri = this.asArray()
            ?.children()
            ?.drop(commands.size)
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString("/", "/") { it.textContent() }
            .orEmpty()
            .let {
                commands.joinToString("/", "/") + it
            }
        return DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri)
    }
}

private object GetMapper : QueryCommand("get")
private object ExpireMapper : QueryCommand("expire")
private object InfoMapper : SingleCommand("info")
private object PingMapper : SingleCommand("ping")
private object CommandMapper : SingleCommand("command")
private object CommandDocsMapper : QueryCommand("command", "docs")
private object DelMapper : SingleCommand("del")

fun JsonElement.toRedisMessage(): RedisMessage =
    when (this) {
        is JsonPrimitive -> toRedisMessage()

        is JsonArray ->
            ArrayRedisMessage(map { it.toRedisMessage() })

        else -> {
            throw IllegalArgumentException("not support type")
        }
    }

fun JsonPrimitive.toRedisMessage(): RedisMessage {
    return if (isString) {
        val buf = Unpooled.buffer()
        buf.writeCharSequence(content, CharsetUtil.UTF_8)
        FullBulkStringRedisMessage(buf)
    } else {
        IntegerRedisMessage(long)
    }
}

private fun String.httpRequest(): HttpRequest {
    return DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/$this")
}

private fun RedisMessage.asArray(): ArrayRedisMessage? {
    return takeIf { it is ArrayRedisMessage }?.let { it as ArrayRedisMessage }
}

private fun RedisMessage.firstString(paramNum: Int): String? =
    asArray()?.takeIf { it.children().size == paramNum }
        ?.takeIf { it.children()[0] is FullBulkStringRedisMessage }
        ?.let { it.children()[0] as FullBulkStringRedisMessage }
        ?.textContent()

private fun RedisMessage.isSingleCommand(command: String): Boolean = command == firstString(1)

@Serializable
data class RespResponseBox<T>(
    val success: Boolean,
    val data: T
)

private fun RedisMessage.textContent(): String =
    when (this) {
        is FullBulkStringRedisMessage ->
            content().toString(CharsetUtil.UTF_8)

        is IntegerRedisMessage -> this.value().toString()
        is SimpleStringRedisMessage ->
            content().toString()

        else -> throw IllegalArgumentException("not a string")
    }
