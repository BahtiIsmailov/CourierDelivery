package com.wb.logistics.network.certificate

import android.content.Context
import com.wb.logistics.R
import com.wb.logistics.utils.LogUtils
import java.io.IOException
import java.io.InputStream
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class CertificateStoreUnsafe(context: Context) : CertificateStoreBase(context) {

    override fun createSslContext(trustManagers: Array<TrustManager>): SSLContext {
        return try {
            val sslContext = SSLContext.getInstance(PROTOCOL)
            sslContext.init(null, trustManagers, SecureRandom())
            sslContext
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun createTrustManager(): Array<TrustManager> {
        val keyStore = createKeyStore(certificateInputStream)
        return try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                ("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers))
            }
            trustManagers
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: KeyStoreException) {
            throw RuntimeException(e)
        }
    }

    private fun createKeyStore(streamCertificate: InputStream): KeyStore {
        return try {
            putAndGetCertificateKeyStore(streamCertificate)
        } catch (throwable: Throwable) {
            throw RuntimeException(throwable)
        }
    }

    @Throws(CertificateException::class, KeyStoreException::class)
    private fun putAndGetCertificateKeyStore(streamCertificate: InputStream): KeyStore {
        val keyStore = emptyKeyStore(defaultPasswordKeyStore)
        for ((index, certificate) in getCertificates(streamCertificate).withIndex()) {
            val certificateAlias = (index).toString()
            keyStore.setCertificateEntry(certificateAlias, certificate)
            LogUtils { logDebug(CertificateStoreUnsafe::class.java, certificateAlias) }
        }
        return keyStore
    }

    @Throws(CertificateException::class)
    private fun getCertificates(streamCertificate: InputStream): Collection<Certificate?> {
        val certificateFactory = CertificateFactory.getInstance(TYPE_CERTIFICATE)
        val certificates = certificateFactory.generateCertificates(streamCertificate)
        require(!certificates.isEmpty()) { "expected non-empty set of trusted certificates" }
        return certificates
    }

    private fun emptyKeyStore(password: CharArray): KeyStore {
        return try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, password)
            keyStore
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw AssertionError(e)
        }
    }

    private val certificateInputStream: InputStream
        get() = context.resources.openRawResource(R.raw.certificate)

    companion object {
        private val defaultPasswordKeyStore: CharArray
            get() = DEFAULT_PASSWORD_KEY_STORE.toCharArray()
    }

}