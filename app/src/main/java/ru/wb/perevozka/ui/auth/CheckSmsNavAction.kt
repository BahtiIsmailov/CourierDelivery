package ru.wb.perevozka.ui.auth

sealed class CheckSmsNavAction {
    object NavigateToApplication : CheckSmsNavAction()
    data class NavigateToUserForm(val phone: String) : CheckSmsNavAction()
    data class NavigateToCompletionRegistration(val phone: String) : CheckSmsNavAction()
}