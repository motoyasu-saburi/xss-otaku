import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
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