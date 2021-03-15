package com.wb.logistics.ui.auth

sealed class NumberPhoneUIState<out R> {
    object Loading : NumberPhoneUIState<Nothing>()
    object NavigateToInput : NumberPhoneUIState<Nothing>()
    object NavigateToTemporaryPassword : NumberPhoneUIState<Nothing>()
    object NavigateToConfig : NumberPhoneUIState<Nothing>()
    object NumberFormatComplete : NumberPhoneUIState<Nothing>()
    object Empty : NumberPhoneUIState<Nothing>()

    data class Success<T>(val data: T) : NumberPhoneUIState<T>()
    data class NumberAttempt(val numberAttempt: String) : NumberPhoneUIState<Nothing>()
    data class NumberNotFound(val numberNotFound: String) : NumberPhoneUIState<Nothing>()
    data class NumberFormat(val number: String) : NumberPhoneUIState<Nothing>()

    data class PhoneNumberNotFound(val message: String) : NumberPhoneUIState<Nothing>()
    data class SMSAuthenticationLocked(val message: String)  : NumberPhoneUIState<Nothing>()
    data class Error(val message: String)  : NumberPhoneUIState<Nothing>()
}