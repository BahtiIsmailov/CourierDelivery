package com.wb.logistics.ui.auth

sealed class NumberPhoneUIState<out R> {
    object PhoneCheck : NumberPhoneUIState<Nothing>()

    object NumberFormatComplete : NumberPhoneUIState<Nothing>()

    data class NumberFormat(val number: String) : NumberPhoneUIState<Nothing>()

    data class NumberNotFound(val message: String) : NumberPhoneUIState<Nothing>()
    data class Error(val message: String) : NumberPhoneUIState<Nothing>()
}