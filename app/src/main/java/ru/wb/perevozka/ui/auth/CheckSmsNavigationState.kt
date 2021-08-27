package ru.wb.perevozka.ui.auth

sealed class CheckSmsNavigationState {
    object NavigateToAppLoader : CheckSmsNavigationState()
}