package ru.wb.go.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.app.AppConfig
import ru.wb.go.network.interceptors.AppMetricResponseInterceptor
import ru.wb.go.network.interceptors.RefreshTokenInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

object OkHttpClientUnsafe {

    @JvmStatic
    fun create(
        httpLoggerInterceptor: HttpLoggingInterceptor,
        authMockResponseInterceptor: ru.wb.go.network.interceptors.AuthMockResponseInterceptor
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(httpLoggerInterceptor)
            .addInterceptor(authMockResponseInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .callTimeout(AppConfig.HTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

    @JvmStatic
    fun create(
        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLoggerInterceptor: HttpLoggingInterceptor,
        appMetricResponseInterceptor: AppMetricResponseInterceptor
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(refreshResponseInterceptor)
            .addInterceptor(httpLoggerInterceptor)
            .addInterceptor(appMetricResponseInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .callTimeout(AppConfig.HTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

    @JvmStatic
    fun create(
        httpLoggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(httpLoggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .callTimeout(AppConfig.HTTP_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

}