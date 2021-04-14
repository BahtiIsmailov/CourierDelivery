package com.wb.logistics.network.headers

import com.wb.logistics.BuildConfig
import com.wb.logistics.network.token.TokenManager
import okhttp3.logging.HttpLoggingInterceptor

object InterceptorFactory {

    fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    fun createHeaderRequestInterceptor(headerManager: HeaderManager): HeaderRequestInterceptor {
        return HeaderRequestInterceptor(headerManager)
    }
}