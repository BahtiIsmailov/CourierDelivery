package com.wb.logistics.ui.auth

sealed class NumberPhoneUIState {
    object PhoneCheck : NumberPhoneUIState()

    object NumberFormatComplete : NumberPhoneUIState()

    data class NumberFormat(val number: String) : NumberPhoneUIState()
    data class NumberFormatInit(val number: String) : NumberPhoneUIState()

    data class NumberNotFound(val message: String) : NumberPhoneUIState()
    data class Error(val message: String) : NumberPhoneUIState()
}