package entity

import java.util.*

data class InterestingLine(
    val line: String, // TODO rename. one line js code
    val variable: List<String>, // TODO rename multiple name
    val source: List<String>, // TODO rename has to ???
    val sink: List<String> // TODO rename has to ???
)

class DomXssAnalyser {

    fun analyse(rawHtml: String): MutableList<InterestingLine> {
        val allControlledVariables = mutableListOf<InterestingLine>()
        val scripts = extractScriptTags(rawHtml)

        // TODO maybe divide to method
        scripts.forEach { script ->
            val scriptsSplitInLines = splitScriptIntoLines(script)
            val linesContainingVar = filterLinesWithNoVariable(scriptsSplitInLines)
            val splitNewLine = splitScriptIntoLines(script)
            val filterVal = filterLinesWithNoVariable(splitNewLine)

            val allVariables = filterVal
                .map { extractVariableNames(it) }
                .filterNotNull()

            val lineContainingVariable = linesContainingVar.filter { line ->
                allVariables.any { v -> line.indexOf(v) != -1 }
            }

            if(lineContainingVariable.isEmpty()) return@forEach // skip

            val sources = linesContainingVar
                .map { extractSourceProcessFromJsCode(it) }
                .filterNotNull()

            val sinks = linesContainingVar
                .map { extractSinkProcessFromJsCode(it) }
                .filterNotNull()

            // TODO Store characteristic variable name
            if(sources.isEmpty() || sinks.isEmpty()) return@forEach // skip

            if(sources.isNotEmpty() && sinks.isNotEmpty()) {
                allControlledVariables.add(
                    InterestingLine(
                        line = script,
                        variable = allVariables, // TODO change to lineContainingVariable,
                        source = sources,
                        sink = sinks
                    )
                )
            }
        }
        return allControlledVariables
    }

    fun extractScriptTags(html: String): List<String> {
        /**
         *  e.g.
         *  IN: <head><script>alert(0);</script></head>
         *  OUT: alert(0)
         */
        val regexOptions = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        val scripts = Regex("(?:<script[^>]*>)(.*?)(?:</script>)", regexOptions)
        val scriptTags = scripts.findAll(html).map { it.groupValues.get(1) }
        return scriptTags.toList()
    }

    fun splitScriptIntoLines(script: String): List<String> {
        return script.split("\n")
    }

    fun filterLinesWithNoVariable(scripts: List<String>): List<String> {
        /**
         * e.g.
         * IN: ["var xx = '123'; \n alert(xx);", "console.log(1);"]
         * OUT: [ "xx = '123'", "alert(xx);" ]
         */
        return scripts.map { it.split("var ") }
            .filter { it.size > 1 }
            .flatten()
            .filter { it != "" }
    }

    fun extractVariableNames(js: String): String? {
        val regex = "[a-zA-Z\$_][a-zA-Z0-9\$_]+".toRegex()
        val variableNames = regex.find(js) ?: return null
        return variableNames.groupValues[0]
    }

    fun extractSourceProcessFromJsCode(js: String): String? {
        val regex: Regex = "document.(URL|documentURI|URLUnencoded|baseURI|cookie|referrer)|location.(href|search|hash|pathname)|window.name|history.(pushState|replaceState)(local|session)Storage".toRegex()
        val source = regex.find(js) ?: return null
        return source.groupValues[0]
    }

    fun extractSinkProcessFromJsCode(js: String): String? {
        val regex: Regex = "eval|evaluate|execCommand|assign|navigate|getResponseHeaderopen|showModalDialog|Function|set(Timeout|Interval|Immediate)|execScript|crypto.generateCRMFRequest|ScriptElement.(src|text|textContent|innerText)|.*?.onEventName|document.(write|writeln)|.*?.innerHTML|Range.createContextualFragment|(document|window).location".toRegex()
        val sink = regex.find(js) ?: return null
        return sink.groupValues[0]
    }
}