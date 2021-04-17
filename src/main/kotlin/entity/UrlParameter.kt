package entity

import java.net.URLEncoder
import java.net.URLDecoder

class UrlParameter(val name: String, val value: String) {

    fun urlEncodeName(encoding: String = "UTF-8"): String {
        return URLEncoder.encode(this.name, encoding)
    }
    fun urlEncodeValue(encoding: String = "UTF-8"): String {
        return URLEncoder.encode(this.value, encoding)
    }
    fun urlDecodeName(encoding: String = "UTF-8"): String {
        return URLDecoder.decode(this.name, encoding)
    }
    fun urlDecodeValue(encoding: String = "UTF-8"): String {
        return URLDecoder.decode(this.value, encoding)
    }

    override fun toString(): String {
        return "${this.name}=${this.value}"
    }
}

