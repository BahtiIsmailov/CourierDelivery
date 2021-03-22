package com.wb.logistics.network.headers

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HeaderRequestInterceptor(private val headerManager: HeaderManager) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        for ((key, value) in headerManager.headerApiMap) {
            builder.addHeader(key, value)
        }
        return chain.proceed(builder.build())
    }
}