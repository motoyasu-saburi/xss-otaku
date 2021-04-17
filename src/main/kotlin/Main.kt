import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import entity.UrlParameter
import java.net.*
import java.io.InputStreamReader

import java.io.BufferedReader
import java.lang.StringBuilder


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val full_url = "http://example.com:80/docs/books/tutorial/index.html?name=networking&name2=123#DOWNLOADING"
        val httpAsync = full_url.httpGet()
            .responseString { request, response, result ->
                val url = URL(full_url)
                val queries = parseQuery(url)

                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        response.headers.forEach {
                            println("${it.key} :${it.value}")
                        }
                        print(response.data.toString(Charsets.UTF_8))
                        response.body()
                    }
                }
            }
        httpAsync.join()
    }
}

fun isReflectiveBody(parameter: List<UrlParameter>, responseBody: String) {
    
}

fun isReflectiveHeaders(parameter: List<UrlParameter>, responseBody: String) {

}

fun parseQuery(url: URL): List<UrlParameter> {
    if(url.query.isNullOrEmpty()) return listOf()

    return url.query
        .split("&")
        .map{ it.split("=", limit=2) } // if contain two (more) the Equal, ignore two.
        .filter { it[0].isNotEmpty() }
        .map { UrlParameter(it[0], it.getOrElse(1){""}) }
}
