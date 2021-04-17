import com.github.kittinunf.fuel.core.Headers
import com.sun.org.glassfish.gmbal.Description
import entity.UrlParameter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

internal class MainKtTest {

    @Nested
    inner class ParseQuery {

        @Test
        @Description("Parse empty URL parameter")
        fun testEmptyUrlParam() {
            assertEquals(
                parseQuery(URL("http://example.com/")),
                listOf<String>()
            )
        }

        @Test
        @Description("Parse normal parameters")
        fun testNormalParse() {
            assertEquals(
                parseQuery(URL("http://example.com/?hoge=123")),
                listOf(UrlParameter("hoge", "123"))
            )
            assertEquals(
                parseQuery(URL("http://example.com/?hoge=123&foo=456+")),
                listOf(UrlParameter("hoge", "123"), UrlParameter("foo", "456+"))
            )
        }

        @Test
        @Description("Parse: Multiple delimiter, Duplicate Parameter, Delimiters only")
        fun testSpecialCaseParse() {
            assertEquals(
                parseQuery(URL("http://example.com/???")),
                listOf(UrlParameter("??", ""))
            )
            assertEquals(
                parseQuery(URL("http://example.com/?=")),
                listOf<UrlParameter>()
            )
            assertEquals(
                parseQuery(URL("http://example.com/?nameDuplicate=123&nameDuplicate=456")),
                listOf(UrlParameter("nameDuplicate", "123"), UrlParameter("nameDuplicate", "456"))
            )
            assertEquals(
                parseQuery(URL("http://example.com/?same=same&same=same")),
                listOf(UrlParameter("same", "same"), UrlParameter("same", "same"))
            )

            assertEquals(
                parseQuery(URL("http://example.com/?hoge=hoge=hoge=hoge")),
                listOf(UrlParameter("hoge", "hoge=hoge=hoge"))
            )
        }
    }

    @Test
    fun testIsReflectiveBody() {
        val inputHtml = """ReflectA
            <!DOCTYPE html>
            <html>
            <body>
                ReflectB
            </body>
            </html>
            ReflectC
        """.trimIndent()

        val inputParams1 = listOf(UrlParameter("key", "ReflectA"))
        val inputParams2 = listOf(UrlParameter("key", "ReflectB"))
        val inputParams3 = listOf(UrlParameter("key", "ReflectC"))
        val inputParams4 = listOf(UrlParameter("key", "ReflectC"), UrlParameter("key2", "ReflectB)"))
        assertTrue(isReflectiveBody(inputHtml, inputParams1))
        assertTrue(isReflectiveBody(inputHtml, inputParams2))
        assertTrue(isReflectiveBody(inputHtml, inputParams3))
        assertTrue(isReflectiveBody(inputHtml, inputParams4))

        val inputParams5 = listOf(UrlParameter("not_exists", "hoge"))
        val inputParams6 = listOf<UrlParameter>()
        assertFalse(isReflectiveBody(inputHtml, inputParams5))
        assertFalse(isReflectiveBody(inputHtml, inputParams6))
        val inputParams7 = listOf(UrlParameter("ReflectA", "ignore_key"))
        assertFalse(isReflectiveBody(inputHtml, inputParams7))
    }

    @Test
    fun testIsReflectiveHeaders() {
        val inputHeaders = Headers()
        inputHeaders
            .append("key", "ReflectA")
            .append("key2", "ReflectB")
            .append("key2", "ReflectC")

        val existsVal1 = listOf(UrlParameter("hoge", "ReflectA"))
        val existsVal2 = listOf(UrlParameter("hoge", "ReflectB"))
        val existsVal3 = listOf(UrlParameter("hoge", "ReflectC"))
        assertTrue(isReflectiveHeaders(inputHeaders, existsVal1))
        assertTrue(isReflectiveHeaders(inputHeaders, existsVal2))
        assertTrue(isReflectiveHeaders(inputHeaders, existsVal3))

        val existsVal4 = listOf(UrlParameter("notExists", "foo"))
        assertFalse(isReflectiveHeaders(inputHeaders, existsVal4))
        val ignoreKey1 = listOf(UrlParameter("ReflectA", "foo"))
        assertFalse(isReflectiveHeaders(inputHeaders, ignoreKey1))
        val ignoreKey2 = listOf(UrlParameter("key", "foo"))
        assertFalse(isReflectiveHeaders(inputHeaders, ignoreKey2))

    }
}