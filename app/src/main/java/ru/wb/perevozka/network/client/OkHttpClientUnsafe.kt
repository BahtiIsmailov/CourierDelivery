package ru.wb.perevozka.network.client

import ru.wb.perevozka.app.AppConfig
import ru.wb.perevozka.network.certificate.CertificateStore
import ru.wb.perevozka.network.headers.RefreshTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.perevozka.network.headers.AppMockResponseInterceptor
import ru.wb.perevozka.network.headers.AuthMockResponseInterceptor
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
        appMockResponseInterceptor: AppMockResponseInterceptor
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                certificateStore.sslSocketFactory(),
                certificateStore.x509TrustManager()
            )
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
            .addInterceptor(refreshResponseInterceptor)
            .addInterceptor(httpLoggerInterceptor)
            .addInterceptor(appMockResponseInterceptor)
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
        return okHttpBuilder.build()
    }

}