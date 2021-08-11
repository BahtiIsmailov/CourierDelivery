package ru.wb.perevozka.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.perevozka.BuildConfig
import ru.wb.perevozka.network.certificate.CertificateStore
import ru.wb.perevozka.network.headers.MockResponseInterceptor
import ru.wb.perevozka.network.headers.RefreshTokenInterceptor

object OkHttpFactory {

    fun createAuthOkHttpClient(
        certificateStore: CertificateStore,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        mockResponseInterceptor: MockResponseInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                certificateStore,
                httpLoggingInterceptor,
                mockResponseInterceptor
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
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(
                certificateStore,
                refreshResponseInterceptor,
                httpLogginInterceptor,
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