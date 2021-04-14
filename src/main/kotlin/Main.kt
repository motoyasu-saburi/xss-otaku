import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.net.*
import java.util.*


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

fun parseParams(queries: String): List<Pair<String, Optional<String>>> {

    return queries.split("&").map{ query ->
        val keyVal = query.split("=")
        keyVal[0] to Optional.of(keyVal[1])
    }
}