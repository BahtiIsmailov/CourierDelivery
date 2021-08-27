package ru.wb.perevozka.ui.auth

sealed class AuthLoaderNavigationState {
    object NavigateToNumberPhone : AuthLoaderNavigationState()
}