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
}