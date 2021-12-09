package ru.wb.go.ui.auth

sealed class CheckSmsNavigationState {
    object NavigateToAppLoader : CheckSmsNavigationState()
}