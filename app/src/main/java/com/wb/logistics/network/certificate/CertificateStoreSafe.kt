package com.wb.logistics.network.certificate

import android.annotation.SuppressLint
import android.content.Context
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class CertificateStoreSafe(context: Context) : CertificateStoreBase(context) {

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    override fun createSslContext(trustManagers: Array<TrustManager>): SSLContext {
        val sslContext = SSLContext.getInstance(PROTOCOL)
        sslContext.init(null, trustManagers, SecureRandom())
        return sslContext
    }

    override fun createTrustManager(): Array<TrustManager> {
        return arrayOf(
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
    }

}