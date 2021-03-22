package com.wb.logistics.network.client

import com.wb.logistics.BuildConfig
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.headers.HeaderRequestInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object OkHttpFactory {
    fun createOkHttpClient(
        certificateStore: CertificateStore,
        requestInterceptor: HeaderRequestInterceptor,
        httpLogginInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            OkHttpClientUnsafe.create(certificateStore, requestInterceptor, httpLogginInterceptor)
        } else {
            OkHttpClientSafe.create(certificateStore, requestInterceptor, httpLogginInterceptor)
        }
    }
}