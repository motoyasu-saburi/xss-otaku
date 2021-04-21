import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer



class MockServerTest {
    private val webServer = MockWebServer()

    fun main() {
        webServer.start(8080)
    }

    private val successResponse = MockResponse().apply {
        setResponseCode(200)
        setHeader("Content-Type", "application/json")
        setHeader("Server", "Cowboy")
        setBody(sampleSuccessResponseData)
    }

    private val failResponse = MockResponse().apply {
        setResponseCode(401)
        setHeader("Content-Type", "application/json")
        setBody(sampleFailedResponseData)
    }

    private val sampleSuccessResponseData = """
{
  "hoge": "fuga"
}
""".trimIndent()

    private val sampleFailedResponseData = """
{
  "err": "unauthorized"
}
""".trimIndent()
}