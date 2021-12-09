package ru.wb.go.network.exceptions

import android.content.Context
import ru.wb.go.R

class ErrorResolutionResourceProviderImpl(private val context: Context) :
    ErrorResolutionResourceProvider {
    override val noInternetError: String
        get() = context.getString(R.string.no_internet_error)
    override val timeoutServiceError: String
        get() = context.getString(R.string.timeout_service_error)
    override val wrongIdentityError: String
        get() = context.getString(R.string.wrong_identity_error)
    override val unauthorizedError: String
        get() = context.getString(R.string.session_error)
    override val unknownError: String
        get() = context.getString(R.string.unknown_error)
    override val unknownHttpError: String
        get() = context.getString(R.string.unknown_http_error)
}