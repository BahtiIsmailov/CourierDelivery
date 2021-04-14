package com.wb.logistics.network.client

import com.wb.logistics.BuildConfig
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.headers.RefreshTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object OkHttpFactory {

    fun createAuthOkHttpClient(
        certificateStore: CertificateStore,
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(certificateStore, httpLoggingInterceptor)
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
            OkHttpClientUnsafe.create(certificateStore,
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