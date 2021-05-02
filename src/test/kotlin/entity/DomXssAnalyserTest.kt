package entity

import io.kotest.core.spec.style.WordSpec
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*


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
//        "extractScriptTags()" When {
//            "extract <script> data from raw HTML string" should {
//                "xxx" {
//                    assertEquals(
//                        listOf("<script>alert(0)</script>"),
//                        dxa.extractScriptTags(genHtml("<script>alert(0)</script>"))
//                    )
//                }
//            }
//        }

        "analyse()" When {
//            "input normal html" should {
//                "no detect" {
//                    assertEquals(dxa.analyse(genHtml("")), emptyList<InterestingLine>())
//                    assertEquals(dxa.analyse(genHtml("<script></script>")), emptyList<InterestingLine>())
//                    assertEquals(dxa.analyse(genHtml("<script>alert(0)</script>")), emptyList<InterestingLine>())
//                    assertEquals(dxa.analyse(genHtml("<script>var xxx = 'wow'; alert(xxx);</script>")), emptyList<InterestingLine>())
//                }
//            }
//            "input no detected script" should {
//                "no detect" {
//                    assertEquals(dxa.analyse(genHtml("<script>var url = location.href; eval(url);</script>")), emptyList<InterestingLine>())
//                }
//            }

            "input detect script" should {
                assertEquals(
//                    dxa.analyse(genHtml("<script type='text/javascript'>var url = location.href; eval(url);</script>")),
                    dxa.analyse(genHtml("<script>var url = location.href; eval(url);</script>")),
                    1
                    //emptyList<InterestingLine>()
                )

            }
        }
    }
}