package ru.wb.perevozka.network.certificate

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface CertificateStore {
    fun sslSocketFactory(): SSLSocketFactory
    fun x509TrustManager(): X509TrustManager
}