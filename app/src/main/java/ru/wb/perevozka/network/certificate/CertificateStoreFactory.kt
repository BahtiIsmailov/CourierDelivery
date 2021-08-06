package ru.wb.perevozka.network.certificate

import android.content.Context
import ru.wb.perevozka.BuildConfig

object CertificateStoreFactory {
    fun createCertificateStore(context: Context): CertificateStore {
        return if (BuildConfig.DEBUG)
            CertificateStoreSafe(context) else CertificateStoreUnsafe(context)
    }
}