package ru.wb.go.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.BuildConfig
import ru.wb.go.network.interceptors.AppMetricResponseInterceptor
import ru.wb.go.network.interceptors.AuthMockResponseInterceptor
import ru.wb.go.network.interceptors.RefreshTokenInterceptor

object OkHttpFactory {

    fun createAuthOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authMockResponseInterceptor: AuthMockResponseInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                httpLoggingInterceptor,
                authMockResponseInterceptor
            )
        } else {
            OkHttpClientSafe.create(httpLoggingInterceptor)
        }
    }

    fun createTokenRefreshOkHttpClient(): OkHttpClient {
        val headerInterceptor = HttpLoggingInterceptor()
        headerInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        val bodyInterceptor = HttpLoggingInterceptor()
        bodyInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(bodyInterceptor)
            .build()
    }

    fun createAppOkHttpClient(

        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLogginInterceptor: HttpLoggingInterceptor,
        appMetricResponseInterceptor: AppMetricResponseInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                refreshResponseInterceptor,
                httpLogginInterceptor,
                appMetricResponseInterceptor
            )
        } else {
            OkHttpClientSafe.create(
                refreshResponseInterceptor,
                httpLogginInterceptor,
            )
        }
    }

    fun createAppOkHttpDemoClient(
        httpLogginInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(httpLogginInterceptor)
        } else {
            OkHttpClientSafe.create(httpLogginInterceptor)
        }
    }
}