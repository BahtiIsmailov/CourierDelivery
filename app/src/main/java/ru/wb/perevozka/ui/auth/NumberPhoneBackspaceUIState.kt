package ru.wb.perevozka.ui.auth

sealed class NumberPhoneBackspaceUIState {
    object Inactive : NumberPhoneBackspaceUIState()
    object Active : NumberPhoneBackspaceUIState()
}