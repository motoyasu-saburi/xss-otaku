package entity

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Assertions.*
import java.util.*

class DomXssAnalyserTest: WordSpec() {
    val dxa = DomXssAnalyser()

    fun genHtml(payload: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <body>

            <h1>My First Heading</h1>
            <p>My first paragraph.</p>
            $payload
            </body>
            </html>
        """.trimIndent()
    }

    init {
        "analyse()" When {
            "input no detected script" should {
                "no detect" {
                    assertEquals(
                        dxa.analyse(genHtml("<script>var url = location.href; eval(url);</script>")),
                        listOf(InterestingLine(
                            line="var url = location.href; eval(url);",
                            variable = listOf("url"),
                            sink = listOf("eval"),
                            source = listOf("location.href")
                        ))
                    )
                }
            }

            "input normal html" should {
                "no detect" {
                    assertEquals(dxa.analyse(genHtml("")), emptyList<InterestingLine>())
                    assertEquals(dxa.analyse(genHtml("<script></script>")), emptyList<InterestingLine>())
                    assertEquals(dxa.analyse(genHtml("<script>alert(0)</script>")), emptyList<InterestingLine>())
                    assertEquals(
                        dxa.analyse(genHtml("<script>var xxx = 'wow'; alert(xxx);</script>")),
                        emptyList<InterestingLine>()
                    )
                }
            }
        }

        "extractScriptTags()" When {
            "extract <script> data from raw HTML string" should {
                "xxx" {
                    listOf("alert(0)").shouldBe(
                        dxa.extractScriptTags(genHtml("<script>alert(0)</script>"))
                    )

                }
            }
        }

        "extractSourceProcessFromJsCode()" When {
            "extract from code containing the Source (e.g. location.href) process" should {
                "extract it" {
                    dxa.extractSourceProcessFromJsCode("var x = location.href; alert(x);")
                        .shouldBe("location.href")
                }
            }
        }

        "extractSinkProcessFromJsCode()" When {
            "extract from code containing the Sink (e.g. eval) process" should {
                "extract it" {
                    dxa.extractSinkProcessFromJsCode("var message = 'abc'; eval(message);")
                        .shouldBe("eval")
                }
            }
        }

    }
}