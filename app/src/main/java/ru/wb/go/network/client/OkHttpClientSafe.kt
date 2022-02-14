package ru.wb.go.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.app.AppConfig
import ru.wb.go.network.certificate.CertificateStore
import ru.wb.go.network.interceptors.RefreshTokenInterceptor
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
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
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
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

}