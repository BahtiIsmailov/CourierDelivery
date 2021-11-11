package ru.wb.go.ui.auth

sealed class NumberPhoneNavAction {
    data class NavigateToCheckPassword(val number: String, val ttl : Int) : NumberPhoneNavAction()
    object NavigateToConfig : NumberPhoneNavAction()
}