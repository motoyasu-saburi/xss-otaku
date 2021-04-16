import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.net.*


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val url = URL("http://example.com:80/docs/books/tutorial/index.html?name=networking&name2=123#DOWNLOADING")
        val queries = url.query
        val params = queries.split("&").map{it.split("=")}

        val httpAsync = "https://httpbin.org/get".httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()
                        println(data)
                    }
                }
            }

        httpAsync.join()
    }
}

fun parseQuery(url: URL): List<List<String?>> {
    if(url.query.isNullOrEmpty()) return listOf()

    // TODO List<List<...>> --> List<Case_Class<>>
    return url.query
        .split("&")
        .map{ it.split("=", limit=2) } // if contain two (more) the Equal, ignore two.
        .filter { it[0].isNotEmpty() }
}



// TODO : Url Param Parse
//     https://www.monotalk.xyz/blog/java-url%E6%96%87%E5%AD%97%E5%88%97%E3%81%8B%E3%82%89%E3%82%AF%E3%82%A8%E3%83%AA%E3%82%B9%E3%83%88%E3%83%AA%E3%83%B3%E3%82%B0%E3%82%92%E5%8F%96%E5%BE%97/
//// TODO change to safety library (api)
//fun splitQuery(url: URL): Map<String?, List<String?>?>? {
//    return if(url.query.isNullOrEmpty()) {
//        Collections.emptyMap()
//    } else {
//        url.query.split("&").map { it ->
//            splitQueryParameter(it)
//        }.toCollection(Collectors.groupingBy(
//            Function<T, K> { obj: T -> obj.key },
//            Supplier<M> { LinkedHashMap() }, mapping(Function<T, U> { java.util.Map.Entry.value }, toList())
//        )
//    }
//}
//
//fun splitQueryParameter(it: String): SimpleImmutableEntry<String?, String?>? {
//    val idx = it.indexOf("=")
//    val key = if (idx > 0) it.substring(0, idx) else it
//    val value = if (idx > 0 && it.length > idx + 1) it.substring(idx + 1) else null
//    return SimpleImmutableEntry(
//        URLDecoder.decode(key, "UTF-8"),
//        URLDecoder.decode(value, "UTF-8")
//    )
//}
