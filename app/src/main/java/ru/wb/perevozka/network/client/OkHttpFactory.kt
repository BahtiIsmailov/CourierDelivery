package ru.wb.perevozka.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.perevozka.BuildConfig
import ru.wb.perevozka.network.certificate.CertificateStore
import ru.wb.perevozka.network.headers.AppMockResponseInterceptor
import ru.wb.perevozka.network.headers.AuthMockResponseInterceptor
import ru.wb.perevozka.network.headers.RefreshTokenInterceptor

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
        appMockResponseInterceptor: AppMockResponseInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                certificateStore,
                refreshResponseInterceptor,
                httpLogginInterceptor,
                appMockResponseInterceptor
            )
        } else {
            OkHttpClientSafe.create(
                certificateStore,
                refreshResponseInterceptor,
                httpLogginInterceptor,
            )
        }
    }
}