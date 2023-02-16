package io.github.guohao.portifi
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryStringEncodeTest {
    @Test
    fun testEncodeAndDecode() {
        val input = "a:= b"
        val encoded = URLEncoder.encode(input, "utf-8")
        val decoded = URLDecoder.decode(encoded, "utf-8")
        assertEquals(input, decoded)
    }
}
