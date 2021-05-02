package entity

data class InterestingLine(
    val line: String, // TODO rename. one line js code
    val variable: List<String>, // TODO rename multiple name
    val hasSource: List<String>, // TODO rename has to ???
    val hasSink: List<String> // TODO rename has to ???
)

class DomXssAnalyser {

    // TODO fix to immutable
    val controlledVariables = mutableListOf<String>() // maybe Not need


    fun analyse(rawHtml: String): MutableList<InterestingLine> {
        val allControlledVariables = mutableListOf<InterestingLine>()

        val scripts = extractScriptTags(rawHtml)

        val allVariables = scripts.map { script ->
            val splitNewLine = splitScriptIntoLines(script)
            val filterVal = filterLinesWithNoVariable(splitNewLine)
            val result = filterVal.map { extractVariableNames(it) }
            result
        }

        // TODO maybe divide to method
        scripts.map { script ->
            val scriptsSplitInLines = splitScriptIntoLines(script)
            val linesContainingVar = filterLinesWithNoVariable(scriptsSplitInLines)

            val lineContainingVariable = allVariables.filter { linesContainingVar.contains(it) }
            if(lineContainingVariable.isEmpty()) {
               // TODO skip
            }

            val sources = linesContainingVar.flatMap { extractSourceProcessFromJsCode(it) }
            val sinks = linesContainingVar.flatMap { extractSinkProcessFromJsCode(it) }

            if(sources.isNotEmpty() || sinks.isNotEmpty()) {
            }

            if(sources.isNotEmpty() && sinks.isNotEmpty()) {
                allControlledVariables.add(
                    InterestingLine(
                        line = script,
                        variable = listOf(""),//lineContainingVariable,
                        hasSource = sources,
                        hasSink = sinks
                    )
                )
                print(lineContainingVariable)
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
        return script.split("\n", "; ", ");")
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

    fun extractVariableNames(script: String): String {
        val regex = "[a-zA-Z\$_][a-zA-Z0-9\$_]+".toRegex()
        val variableNames = regex.findAll(script).map { it.value }
        return variableNames.//.get(1).replace("$", "\$")
    }

    fun extractSourceProcessFromJsCode(js: String): List<String> {
        val sources: Regex = "document.(URL|documentURI|URLUnencoded|baseURI|cookie|referrer)|location.(href|search|hash|pathname)|window.name|history.(pushState|replaceState)(local|session)Storage".toRegex()
        val sourceMatches = sources.matchEntire(js) ?: return listOf()

        // Memo: maybe change Any String to Single String
        return sourceMatches.groupValues
    }

    fun extractSinkProcessFromJsCode(js: String): List<String> {
        val sinks: Regex = "eval|evaluate|execCommand|assign|navigate|getResponseHeaderopen|showModalDialog|Function|set(Timeout|Interval|Immediate)|execScript|crypto.generateCRMFRequest|ScriptElement.(src|text|textContent|innerText)|.*?.onEventName|document.(write|writeln)|.*?.innerHTML|Range.createContextualFragment|(document|window).location".toRegex()
        val sinkMatches = sinks.matchEntire(js) ?: return listOf()

        // Memo: maybe change Any String to Single String
        return sinkMatches.groupValues
    }

    fun extractCharacteristicJsCode() {

    }
}