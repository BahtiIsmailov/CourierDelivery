package ru.wb.go.network.certificate

import android.content.Context
import ru.wb.go.BuildConfig

object CertificateStoreFactory {
    fun createCertificateStore(context: Context): CertificateStore {
        return if (BuildConfig.DEBUG)
            CertificateStoreSafe(context) else CertificateStoreUnsafe(context)
    }
}