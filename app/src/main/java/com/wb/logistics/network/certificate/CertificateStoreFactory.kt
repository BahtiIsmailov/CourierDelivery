package com.wb.logistics.network.certificate

import android.content.Context
import com.wb.logistics.BuildConfig

object CertificateStoreFactory {
    fun createCertificateStore(context: Context): CertificateStore {
        return if (BuildConfig.DEBUG)
            CertificateStoreSafe(context) else CertificateStoreUnsafe(context)
    }
}