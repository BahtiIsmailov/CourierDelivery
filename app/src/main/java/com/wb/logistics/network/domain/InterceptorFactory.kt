package com.wb.logistics.network.domain

import com.wb.logistics.BuildConfig
import com.wb.logistics.network.headers.HeaderManager
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