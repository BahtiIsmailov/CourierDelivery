package ru.wb.go.network.interceptors

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import ru.wb.go.utils.RebootDialogManager
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class AppMetricResponseInterceptor() : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)

        if (response.code == 409) {
            RebootDialogManager.showRebootDialog(409)
        }
        if (response.code >= 500){
            RebootDialogManager.showRebootDialog(500)
        }
        val url = request.url.toString()
        val singleApiMethod = "<-" + getSingleApiMethod(url)
        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"

        val source = responseBody.source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer

        val contentType = responseBody.contentType()
        val charset: Charset =
            contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

        val out = if (contentLength == 0L) {
            bodySize
        } else {
            val readSource = buffer.clone().readString(charset)
            val trimSource =
                if (readSource.length > 20) readSource.take(20) + "(trim)..." else readSource
            trimSource + bodySize
        }

        val responseHeaders = response.headers
        val firstItemHeader = if (responseHeaders.size > 0) {
            responseHeaders.name(0) + ": " + responseHeaders.value(0)
        } else {
            "Headers is empty"
        }

        val codeOut =
            "response code " + response.code + " firstItemHeader " + firstItemHeader + " " + out
        //metric.onTechNetworkLog(singleApiMethod, codeOut)
        return response
    }


    private fun getSingleApiMethod(url: String): String {
        return url.substring(url.lastIndexOf(SLASH_SYMBOL) + 1)
    }

    companion object {
        private const val SLASH_SYMBOL = "/"
    }

}