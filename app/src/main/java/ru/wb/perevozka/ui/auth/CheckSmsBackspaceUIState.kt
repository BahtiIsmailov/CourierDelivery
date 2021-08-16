package ru.wb.perevozka.ui.auth

sealed class CheckSmsBackspaceUIState {
    object Inactive : CheckSmsBackspaceUIState()
    object Active : CheckSmsBackspaceUIState()
}