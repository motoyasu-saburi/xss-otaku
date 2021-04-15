import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.net.*
import java.util.*
import java.net.URLDecoder

import java.util.AbstractMap.SimpleImmutableEntry

import java.util.stream.Collectors.mapping

import java.util.LinkedHashMap

import java.util.stream.Collectors

import java.util.Arrays
import java.util.function.Function
import java.util.function.Supplier


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

// TODO change to safety library (api)
fun splitQuery(url: URL): Map<String?, List<String?>?>? {
    return if(url.query.isNullOrEmpty()) {
        Collections.emptyMap()
    } else {
        url.query.split("&").map {
            splitQueryParameter(it)
        }.groupBy {
            Function<T, K> { obj: T -> obj.key },
            Supplier<M> { LinkedHashMap() }, mapping(Function<T, U> { java.util.Map.Entry.value }, toList())
        }
    }
//    } else Arrays.stream(url.query.split("&"))
//        .map { it: String? -> this.splitQueryParameter(it) }
//        .collect(Collectors.groupingBy(
//            Function<T, K> { obj: T -> obj.key },
//            Supplier<M> { LinkedHashMap() }, mapping(Function<T, U> { java.util.Map.Entry.value }, toList())
//        )
//        )
}

fun splitQueryParameter(it: String): SimpleImmutableEntry<String?, String?>? {
    val idx = it.indexOf("=")
    val key = if (idx > 0) it.substring(0, idx) else it
    val value = if (idx > 0 && it.length > idx + 1) it.substring(idx + 1) else null
    return SimpleImmutableEntry(
        URLDecoder.decode(key, "UTF-8"),
        URLDecoder.decode(value, "UTF-8")
    )
}
