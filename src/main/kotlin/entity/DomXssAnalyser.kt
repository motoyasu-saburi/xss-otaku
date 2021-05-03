package entity

import java.util.*

data class InterestingLine(
    val line: String, // TODO rename. one line js code
    val variable: List<String>, // TODO rename multiple name
    val source: List<String>, // TODO rename has to ???
    val sink: List<String> // TODO rename has to ???
)

class DomXssAnalyser {

    // TODO fix to immutable
    val controlledVariables = mutableListOf<String>() // maybe Not need


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
                .filter { it.isPresent }
                .map { it.get() }

            // TODO bug
            val lineContainingVariable = linesContainingVar.filter { line ->
                allVariables.any { v -> line.indexOf(v) != -1 }
            }
            if(lineContainingVariable.isEmpty()) {
                return@forEach // Skip
            }

            val sources = linesContainingVar
                .map { extractSourceProcessFromJsCode(it) }
                .filter { it.isPresent }
                .map { it.get() }

            val sinks = linesContainingVar
                .map { extractSinkProcessFromJsCode(it) }
                .filter { it.isPresent }
                .map { it.get() }

            if(sources.isEmpty() || sinks.isEmpty()) {
                // TODO Store characteristic variable name
                return@forEach // skip
            }

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
        allControlledVariables.forEach {
            print(it)
        }
        return allControlledVariables
    }

//    fun formatHtml(rawHtml: String) {
        // TODO?
//        検知前の整形処理で method chain, Object の改行部分を整形し直す
//        変数を先に抽出する処理を行なってから Sink, Source の検知を行う （ XSStrike では合わせて抽出処理を行なっているため、コード前半に問題があるケースでは漏らしそうなイメージ。もしかしたらズレてるかも）
//        sink, source の regex の改善 (検知タイプの追加）
//    }

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
//        return script.split("\n", "; ", ");")    TODO remove (or tuning)  "; ", ");"
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

    fun extractVariableNames(js: String): Optional<String> {
        val regex = "[a-zA-Z\$_][a-zA-Z0-9\$_]+".toRegex()
        val variableNames = regex.find(js) ?: return Optional.empty<String>()
        return Optional.of(variableNames.groupValues[0])
    }

    fun extractSourceProcessFromJsCode(js: String): Optional<String> {
        val regex: Regex = "document.(URL|documentURI|URLUnencoded|baseURI|cookie|referrer)|location.(href|search|hash|pathname)|window.name|history.(pushState|replaceState)(local|session)Storage".toRegex()
        val source = regex.find(js) ?: return Optional.empty<String>()
        return Optional.of(source.groupValues[0])
    }

    fun extractSinkProcessFromJsCode(js: String): Optional<String> {
        val regex: Regex = "eval|evaluate|execCommand|assign|navigate|getResponseHeaderopen|showModalDialog|Function|set(Timeout|Interval|Immediate)|execScript|crypto.generateCRMFRequest|ScriptElement.(src|text|textContent|innerText)|.*?.onEventName|document.(write|writeln)|.*?.innerHTML|Range.createContextualFragment|(document|window).location".toRegex()
        val sink = regex.find(js) ?: return Optional.empty<String>()
        return Optional.of(sink.groupValues[0])
    }

    fun extractCharacteristicJsCode() {

    }
}