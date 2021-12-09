package ru.wb.go.ui.auth

sealed class CheckSmsBackspaceUIState {
    object Inactive : CheckSmsBackspaceUIState()
    object Active : CheckSmsBackspaceUIState()
}