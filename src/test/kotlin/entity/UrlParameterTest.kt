package entity

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested

internal class UrlParameterTest {

    @Test
    fun urlEncodeName() {
        val u = UrlParameter("/", "slash")
        assertEquals(u.urlEncodeName(), "%2F")
    }

    @Test
    fun urlEncodeValue() {
        val u = UrlParameter("slash", "/")
        assertEquals(u.urlEncodeValue(), "%2F")
    }

    @Test
    fun urlDecodeName() {
        val u = UrlParameter("%2F", "/")
        assertEquals(u.urlDecodeName(), "/")
    }

    @Test
    fun urlDecodeValue() {
        val u = UrlParameter("/", "%2f")
        assertEquals(u.urlDecodeValue(), "/")
    }

    @Nested
    inner class ToString {
        @Test
        fun testToString() {
            val u = UrlParameter("abc", "123")
            assertEquals(u.toString(), "abc=123")
        }
    }
}