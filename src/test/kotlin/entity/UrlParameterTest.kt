package entity

import io.kotest.core.spec.style.StringSpec
import org.junit.jupiter.api.Assertions.*


class UrlParameterTest: StringSpec({
    "urlEncodeName should encode parameter name" {
        val u = UrlParameter("/", "slash")
        assertEquals(u.urlEncodeName(), "%2F")
    }

    "urlEncodeValue should encode parameter value" {
        val u = UrlParameter("slash", "/")
        assertEquals(u.urlEncodeValue(), "%2F")
    }

    "urlDecodeName should decode parameter name" {
        val u = UrlParameter("%2F", "/")
        assertEquals(u.urlDecodeName(), "/")
    }

    "urlDecodeValue should decode parameter value" {
        val u = UrlParameter("/", "%2f")
        assertEquals(u.urlDecodeValue(), "/")
    }

    "toString should return a string consisting of Key and Value concatenated with '='" {
        val u = UrlParameter("abc", "123")
        assertEquals(u.toString(), "abc=123")
    }
})