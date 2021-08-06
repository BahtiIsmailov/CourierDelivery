package ru.wb.perevozka.ui.auth

sealed class InputPasswordNavAction {
    data class NavigateToTemporaryPassword(val phone : String) : InputPasswordNavAction()
    object NavigateToApplication : InputPasswordNavAction()
}