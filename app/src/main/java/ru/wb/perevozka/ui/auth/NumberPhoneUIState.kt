package ru.wb.perevozka.ui.auth

sealed class NumberPhoneUIState {
    object NumberCheckProgress : NumberPhoneUIState()
    object NumberFormatComplete : NumberPhoneUIState()
    object NumberNotFilled : NumberPhoneUIState()

    data class PhoneSpanFormat(val numberFormat: String, val count: Int) : NumberPhoneUIState()

    data class NumberNotFound(val message: String) : NumberPhoneUIState()
    data class Error(val message: String) : NumberPhoneUIState()
}