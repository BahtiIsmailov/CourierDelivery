package com.wb.logistics.ui.auth

sealed class NumberPhoneUIState<out R> {
    object PhoneCheck : NumberPhoneUIState<Nothing>()
    data class NavigateToInputPassword(val number: String) : NumberPhoneUIState<Nothing>()
    data class NavigateToTemporaryPassword(val number: String) : NumberPhoneUIState<Nothing>()

    object NavigateToConfig : NumberPhoneUIState<Nothing>()
    object NumberFormatComplete : NumberPhoneUIState<Nothing>()
    object Empty : NumberPhoneUIState<Nothing>()

    data class NumberFormat(val number: String) : NumberPhoneUIState<Nothing>()

    data class NumberNotFound(val message: String) : NumberPhoneUIState<Nothing>()
    data class Error(val message: String) : NumberPhoneUIState<Nothing>()
}