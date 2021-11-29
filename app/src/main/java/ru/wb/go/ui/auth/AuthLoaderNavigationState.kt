package ru.wb.go.ui.auth

sealed class AuthLoaderNavigationState {
    object NavigateToNumberPhone : AuthLoaderNavigationState()
}