package ru.wb.go.ui.auth

sealed class NumberPhoneUIState {
    object NumberCheckProgress : NumberPhoneUIState()
    object NumberFormatComplete : NumberPhoneUIState()
    object NumberNotFilled : NumberPhoneUIState()

    data class PhoneSpanFormat(val numberFormat: String, val count: Int) : NumberPhoneUIState()

    data class NumberNotFound(val title: String, val message: String, val button: String) :
        NumberPhoneUIState()

    data class Error(val title: String, val message: String, val button: String) : NumberPhoneUIState()
}