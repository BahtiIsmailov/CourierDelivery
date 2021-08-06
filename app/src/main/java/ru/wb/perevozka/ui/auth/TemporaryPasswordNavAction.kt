package ru.wb.perevozka.ui.auth

sealed class TemporaryPasswordNavAction {
    data class NavigateToCreatePassword(val phone: String, val tmpPassword: String) :
        TemporaryPasswordNavAction()
}