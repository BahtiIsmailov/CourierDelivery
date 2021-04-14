package com.wb.logistics.network.client

import com.wb.logistics.app.AppConfig
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.headers.RefreshTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpClientSafe {

    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        refreshResponseInterceptor: RefreshTokenInterceptor,
        loggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .addInterceptor(refreshResponseInterceptor)
            .addInterceptor(loggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        loggerInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .addInterceptor(loggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

}