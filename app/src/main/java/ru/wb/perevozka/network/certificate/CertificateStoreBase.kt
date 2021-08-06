package ru.wb.perevozka.network.certificate

import android.content.Context
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

abstract class CertificateStoreBase(protected var context: Context) : CertificateStore {

    private val sslSocketFactory: SSLSocketFactory
    private val x509TrustManager: X509TrustManager

    abstract fun createSslContext(trustManagers: Array<TrustManager>): SSLContext
    abstract fun createTrustManager(): Array<TrustManager>

    override fun sslSocketFactory(): SSLSocketFactory {
        return sslSocketFactory
    }

    override fun x509TrustManager(): X509TrustManager {
        return x509TrustManager
    }

    init {
        val trustManagers = createTrustManager()
        sslSocketFactory = createSslContext(trustManagers).socketFactory
        x509TrustManager = trustManagers[0] as X509TrustManager
    }

    companion object {
        const val DEFAULT_PASSWORD_KEY_STORE = "password"
        const val TYPE_CERTIFICATE = "X.509"
        const val PROTOCOL = "SSL"
    }

}