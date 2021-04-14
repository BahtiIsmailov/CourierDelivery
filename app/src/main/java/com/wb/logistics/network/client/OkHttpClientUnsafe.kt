package com.wb.logistics.network.client

import com.wb.logistics.app.AppConfig
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.headers.RefreshTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

object OkHttpClientUnsafe {
    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        httpLoggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(httpLoggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLoggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(refreshResponseInterceptor)
            .addInterceptor(httpLoggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

}