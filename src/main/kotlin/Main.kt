import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import entity.UrlParameter
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

fun parseQuery(url: URL): List<UrlParameter> {
    if(url.query.isNullOrEmpty()) return listOf()

    return url.query
        .split("&")
        .map{ it.split("=", limit=2) } // if contain two (more) the Equal, ignore two.
        .filter { it[0].isNotEmpty() }
        .map { UrlParameter(it[0], it.getOrElse(1){""}) }
}
