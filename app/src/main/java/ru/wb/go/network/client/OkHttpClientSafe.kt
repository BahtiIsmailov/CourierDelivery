package ru.wb.go.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.app.AppConfig
import ru.wb.go.network.interceptors.RefreshTokenInterceptor
import java.util.concurrent.TimeUnit

object OkHttpClientSafe {

    @JvmStatic
    fun create(
        refreshResponseInterceptor: RefreshTokenInterceptor,
        loggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(refreshResponseInterceptor)
            .addInterceptor(loggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

    @JvmStatic
    fun create(

        loggerInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .callTimeout(AppConfig.HTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

}