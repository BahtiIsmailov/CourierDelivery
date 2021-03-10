package com.wb.logistics.network.rx

import android.content.Context
import com.wb.logistics.R

class CallAdapterFactoryResourceProviderImpl(private val context: Context) :
    CallAdapterFactoryResourceProvider {
    override val noInternetError: String
        get() = context.getString(R.string.no_internet_error)
    override val timeoutServiceError: String
        get() = context.getString(R.string.timeout_service_error)
    override val wrongIdentityError: String
        get() = context.getString(R.string.wrong_identity_error)
    override val unauthorizedError: String
        get() = context.getString(R.string.session_error)
}