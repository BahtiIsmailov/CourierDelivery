package com.wb.logistics.ui.auth

sealed class NumberPhoneNavAction {
    data class NavigateToInputPassword(val number: String) : NumberPhoneNavAction()
    data class NavigateToTemporaryPassword(val number: String) : NumberPhoneNavAction()
    object NavigateToConfig : NumberPhoneNavAction()
}