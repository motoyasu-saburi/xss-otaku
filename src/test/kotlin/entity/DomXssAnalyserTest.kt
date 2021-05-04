package entity

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

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
            "analyse script containing Source & Sink code" should {
                "return the results of vuln detection class" {

                    dxa.analyse(genHtml("<script>var url = location.href; eval(url);</script>"))
                        .shouldBe(
                            listOf(InterestingLine(
                                line="var url = location.href; eval(url);",
                                variable = listOf("url"),
                                sink = listOf("eval"),
                                source = listOf("location.href")
                            ))
                        )

                    // TODO Make it possible to detect even if the lines are different.
                    val multipleLineScript = "<script>var aaa = \"example\";\nvar url = location.href; eval(url);\nvar bbb = \"xxx\";\n</script>"
                    val expectLine = "var aaa = \"example\";\nvar url = location.href; eval(url);\nvar bbb = \"xxx\";\n"

                    dxa.analyse(multipleLineScript)[0].shouldBe(
                        mutableListOf(InterestingLine(
                            line = expectLine,
                            variable = listOf("aaa", "url", "bbb"),
                            sink = listOf("eval"),
                            source = listOf("location.href")
                        ))[0]
                    )
                }
            }

            "analyse script containing a variable & The Source (not contain Sink)" should {
                "return " {
                    dxa.analyse(genHtml("<script>var xxx = location.href; alert(xxx);</script>")).shouldBe(
                        emptyList()
                    )
                }
            }

            "analyse script not containing The Variable, Source, Sink" should {
                "return empty list" {
                    dxa.analyse(genHtml("")) shouldBe emptyList()
                    dxa.analyse(genHtml("<script></script>")) shouldBe emptyList()
                    dxa.analyse(genHtml("<script>alert(0)</script>")) shouldBe emptyList()
                }
            }

            "analyse script containing Variable & not contain Sink, Source" should {
                "return empty list" {
                    dxa.analyse(genHtml("<script>var xxx = 'wow'; alert(xxx);</script>"))
                        .shouldBe(emptyList())
                }
            }
        }

        "extractScriptTags()" When {
            "extract <script> data from raw HTML string" should {
                "return <script> erased data" {
                    dxa.extractScriptTags(genHtml("<script>alert(0)</script>")).shouldBe(
                        listOf("alert(0)")
                    )
                }
            }
            "extract containing New Line <script> tag" should {
                "return the entire <script> tag, including NewLine(\\n)" {
                    val containingNewLineScriptTag = """
                        <script>
                        alert(0)
                        </script>
                    """.trimIndent()

                    dxa.extractScriptTags(genHtml(containingNewLineScriptTag)).shouldBe(
                        listOf("\nalert(0)\n")
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