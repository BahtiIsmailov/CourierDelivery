package ru.wb.perevozka.ui.auth

sealed class NumberPhoneNavAction {
    data class NavigateToInputPassword(val number: String) : NumberPhoneNavAction()
    data class NavigateToTemporaryPassword(val number: String) : NumberPhoneNavAction()
    object NavigateToConfig : NumberPhoneNavAction()
}