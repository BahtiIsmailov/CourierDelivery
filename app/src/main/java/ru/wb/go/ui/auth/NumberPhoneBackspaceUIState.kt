package ru.wb.go.ui.auth

sealed class NumberPhoneBackspaceUIState {
    object Inactive : NumberPhoneBackspaceUIState()
    object Active : NumberPhoneBackspaceUIState()
}