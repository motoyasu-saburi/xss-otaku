import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import entity.UrlParameter
import java.net.*
import java.util.*


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val fullUrl = "http://example.com:80/docs/books/tutorial/index.html?name=networking&name2=123#DOWNLOADING"
        isReflective(fullUrl)
    }
}


fun combineUrlParams(params: List<UrlParameter>): String {
    // TODO rename method
    return params.fold("", { res, current -> "${res}&${current.name}=${current.value}" })
        .replaceFirst("&", "")
}

//fun genUrlWithEachParamReplaced(fullUrl: String, payloads: List<String>): List<String> {
//    //TODO
//    val url = URL(fullUrl)
//    val fullQueries: String = url.query
//    val queries: List<UrlParameter> = parseQuery(url)
//
//    var replacedFullUrls = []
//    for (q in queries) {
//        queries.toString()
//    }
//
//
//    queries.map { query ->
//        payloads.map { pay ->
//            query.value.replace()
//        }
//    }
//}
//
//fun parameterLoop(fullUrl: String, params: List<UrlParameter>): List<String> {
//    val url = URL(fullUrl)
//    val queries: List<UrlParameter> = parseQuery(url)
//
//    val payloads: List<String> = listOf("payload1", "payload2")
//
//    queries.map {  }
//
//    fullUrl.replace(url.query, "")
//}


fun isReflective(full_url: String): Boolean {
    val (_, response, result) = full_url.httpGet().responseString()

    return when (result) {
        is Result.Failure -> false
        is Result.Success -> {
            val url = URL(full_url)
            val queries = parseQuery(url)

            return isReflectiveResponse(
                response.headers,
                response.data.toString(Charsets.UTF_8),
                queries
            )
        }
        else -> false
    }
}

fun isReflectiveResponse(headers: Headers, body: String, parameters: List<UrlParameter>): Boolean {
    return isReflectiveHeaders(headers, parameters) || isReflectiveBody(body, parameters)
}

fun isReflectiveBody(responseBody: String, parameters: List<UrlParameter>): Boolean {
    return parameters.any{ responseBody.contains(it.value) }
}

fun isReflectiveHeaders(responseHeaders: Headers, parameters: List<UrlParameter>): Boolean {
    val h = responseHeaders.toString()
    return parameters.any{ h.contains(it.value) }
}

fun parseQuery(url: URL): List<UrlParameter> {
    if(url.query.isNullOrEmpty()) return emptyList()

    return url.query
        .split("&")
        .map{ it.split("=", limit=2) } // if contain two (more) the Equal, ignore two. (e.g. foo===)
        .filter { it[0].isNotEmpty() }
        .map { UrlParameter(it[0], it.getOrElse(1){""}) }

}
