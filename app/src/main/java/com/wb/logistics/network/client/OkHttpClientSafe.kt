package com.wb.logistics.network.client

import com.wb.logistics.app.AppConfig
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.domain.HeaderRequestInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpClientSafe {
    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        requestInterceptor: HeaderRequestInterceptor,
        loggerInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .addInterceptor(requestInterceptor)
            .addInterceptor(loggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }
}