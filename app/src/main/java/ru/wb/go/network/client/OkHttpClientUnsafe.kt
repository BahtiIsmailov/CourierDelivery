package ru.wb.go.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.app.AppConfig
import ru.wb.go.network.certificate.CertificateStore
import ru.wb.go.network.headers.AppMetricResponseInterceptor
import ru.wb.go.network.headers.AuthMockResponseInterceptor
import ru.wb.go.network.headers.RefreshTokenInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

object OkHttpClientUnsafe {
    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        httpLoggerInterceptor: HttpLoggingInterceptor,
        authMockResponseInterceptor: AuthMockResponseInterceptor
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(httpLoggerInterceptor)
            .addInterceptor(authMockResponseInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

    @JvmStatic
    fun create(
        certificateStore: CertificateStore,
        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLoggerInterceptor: HttpLoggingInterceptor,
        appMetricResponseInterceptor: AppMetricResponseInterceptor
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(refreshResponseInterceptor)
            .addInterceptor(httpLoggerInterceptor)
            .addInterceptor(appMetricResponseInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

    @JvmStatic
    fun create(
        httpLoggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .addInterceptor(httpLoggerInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

}