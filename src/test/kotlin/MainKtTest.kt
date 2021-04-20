import com.github.kittinunf.fuel.core.Headers
import entity.UrlParameter
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


class MainKtTest: WordSpec({

    "list test" When {
        "test" should {
            "return test" {
                val s = listOf(UrlParameter("hoge", "123"), UrlParameter("foo", "456+"))
                val d = listOf(UrlParameter("hoge", "123"), UrlParameter("foo", "456+"))
                s shouldContainExactly d
            }
        }
    }


    "parseQuery" When {

        "parse no parameter" should {
            "return empty list" {
                parseQuery(URL("http://example.com/")) shouldBe emptyList()
            }
        }


        "parse normal url parameters" should {
            "return List UrlParameter" {
                val actual = parseQuery(URL("http://example.com/?hoge=123"))
                val expect = listOf(UrlParameter("hoge", "123"))
                 actual.shouldContainExactly(expect) //shouldContainInOrder // shouldContainAll  //   //shouldBe


                parseQuery(URL("http://example.com/?hoge=123&foo=456+")) shouldContainExactly
                        listOf(UrlParameter("hoge", "123"), UrlParameter("foo", "456+"))
            }
        }

        "parse delimiter(&=) only url parameters" should {
            "return empty list" {
                parseQuery(URL("http://example.com/?=")) shouldBe listOf()
            }
        }

        "parse '?' only url parameters" should {
            "return the second and subsequent '?' " {
                parseQuery(URL("http://example.com/???")) shouldContainInOrder
                        listOf(UrlParameter("??", ""))
            }
        }

        "parse duplicate key for url parameters" should {
            "return a list with the same name but with independent keys List UrlParameters" {
                parseQuery(
                    URL("http://example.com/?nameDuplicate=123&nameDuplicate=456")
                ) shouldContainInOrder listOf(
                    UrlParameter("nameDuplicate", "123"),
                    UrlParameter("nameDuplicate", "456")
                )

                parseQuery(
                    URL("http://example.com/?same=same&same=same")
                ) shouldContainInOrder listOf(
                    UrlParameter("same", "same"),
                    UrlParameter("same", "same")
                )
            }
        }

        "parse parameters with no '&' and continuous '='" should {
            "return List of UrlParameter separated into key value by the first '='" {
                parseQuery(URL("http://example.com/?hoge=hoge=hoge=hoge")) shouldContainInOrder
                        listOf(UrlParameter("hoge", "hoge=hoge=hoge"))
            }
        }
    }


    "isReflectiveBody" When {
        val inputHtml = """ReflectA
            <!DOCTYPE html>
            <html>
            <body>
                ReflectB
            </body>
            </html>
            ReflectC
            """.trimIndent()

        "inspection response body whose contains request param values" should {
            "return true" {
                val inputParams1 = listOf(UrlParameter("key", "ReflectA"))
                val inputParams2 = listOf(UrlParameter("key", "ReflectB"))
                val inputParams3 = listOf(UrlParameter("key", "ReflectC"))
                val inputParams4 = listOf(
                    UrlParameter("key", "ReflectC"),
                    UrlParameter("key2", "ReflectB)")
                )
                isReflectiveBody(inputHtml, inputParams1) shouldBe true
                isReflectiveBody(inputHtml, inputParams2) shouldBe true
                isReflectiveBody(inputHtml, inputParams3) shouldBe true
                isReflectiveBody(inputHtml, inputParams4) shouldBe true
            }
        }
        "inspection response body whose NOT contains request param values" should {
            "return false" {
                val inputParams5 = kotlin.collections.listOf(entity.UrlParameter("not_exists", "hoge"))
                val inputParams6 = kotlin.collections.listOf<entity.UrlParameter>()
                isReflectiveBody(inputHtml, inputParams5) shouldBe false
                isReflectiveBody(inputHtml, inputParams6) shouldBe false

                val inputParams7 = kotlin.collections.listOf(entity.UrlParameter("ReflectA", "ignore_key"))
                isReflectiveBody(inputHtml, inputParams7) shouldBe false
            }
        }
    }

    "isReflectiveHeaders" When {
        val inputHeaders = Headers()
        inputHeaders
            .append("key", "ReflectA")
            .append("key2", "ReflectB")
            .append("key2", "ReflectC")


        "inspection response header whose contains request param values" should {
            "return true" {
                val existsVal1 = listOf(UrlParameter("hoge", "ReflectA"))
                val existsVal2 = listOf(UrlParameter("hoge", "ReflectB"))
                val existsVal3 = listOf(UrlParameter("hoge", "ReflectC"))

                isReflectiveHeaders(inputHeaders, existsVal1) shouldBe true
                isReflectiveHeaders(inputHeaders, existsVal2) shouldBe true
                isReflectiveHeaders(inputHeaders, existsVal3) shouldBe true
            }
        }
        "inspection response header whose NOT contains request param values" should {
            "return false" {
                val notExists1 = listOf(UrlParameter("notExists", "foo"))
                isReflectiveHeaders(inputHeaders, notExists1) shouldBe false
                val notExists2 = listOf(UrlParameter("ReflectA", "foo"))
                isReflectiveHeaders(inputHeaders, notExists2) shouldBe false
                val notExists3 = listOf(UrlParameter("key", "foo"))
                isReflectiveHeaders(inputHeaders, notExists3) shouldBe false
            }
        }
    }

    "isRefrectiveResponse" When {
        // TODO
    }
})

