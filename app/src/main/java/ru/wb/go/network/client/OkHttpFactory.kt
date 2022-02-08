package ru.wb.go.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.BuildConfig
import ru.wb.go.network.certificate.CertificateStore
import ru.wb.go.network.headers.AppMetricResponseInterceptor
import ru.wb.go.network.headers.AuthMockResponseInterceptor
import ru.wb.go.network.headers.RefreshTokenInterceptor

object OkHttpFactory {

    fun createAuthOkHttpClient(
        certificateStore: CertificateStore,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authMockResponseInterceptor: AuthMockResponseInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                certificateStore,
                httpLoggingInterceptor,
                authMockResponseInterceptor
            )
        } else {
            OkHttpClientSafe.create(certificateStore, httpLoggingInterceptor)
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
        certificateStore: CertificateStore,
        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLogginInterceptor: HttpLoggingInterceptor,
        appMetricResponseInterceptor: AppMetricResponseInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                certificateStore,
                refreshResponseInterceptor,
                httpLogginInterceptor,
                appMetricResponseInterceptor
            )
        } else {
            OkHttpClientSafe.create(
                certificateStore,
                refreshResponseInterceptor,
                httpLogginInterceptor,
            )
        }
    }

    fun createAppOkHttpDemoClient(
        certificateStore: CertificateStore,
        httpLogginInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(certificateStore, httpLogginInterceptor)
        } else {
            OkHttpClientSafe.create(certificateStore, httpLogginInterceptor)
        }
    }
}