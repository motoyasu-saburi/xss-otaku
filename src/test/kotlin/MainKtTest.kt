import com.sun.org.glassfish.gmbal.Description
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
                listOf(listOf("hoge", "123"))
            )
            assertEquals(
                parseQuery(URL("http://example.com/?hoge=123&foo=456+")),
                listOf(listOf("hoge", "123"), listOf("foo", "456+"))
            )
        }

        @Test
        @Description("Parse: Multiple delimiter, Duplicate Parameter, Delimiters only")
        fun testSpecialCaseParse() {
            assertEquals(
                parseQuery(URL("http://example.com/???")),
                listOf(listOf("??"))
            )
            assertEquals(
                parseQuery(URL("http://example.com/?=")),
                listOf<String>()
            )
            assertEquals(
                parseQuery(URL("http://example.com/?nameDuplicate=123&nameDuplicate=456")),
                listOf(listOf("nameDuplicate", "123"), listOf("nameDuplicate", "456"))
            )
            assertEquals(
                parseQuery(URL("http://example.com/?same=same&same=same")),
                listOf(listOf("same", "same"), listOf("same", "same"))
            )

            assertEquals(
                parseQuery(URL("http://example.com/?hoge=hoge=hoge=hoge")),
                listOf(listOf("hoge", "hoge=hoge=hoge"))
            )
        }
    }
}