package ru.wb.perevozka.ui.auth

sealed class CheckSmsUIState {
    object Progress : CheckSmsUIState()
    object Complete : CheckSmsUIState()
    data class CodeFormat(val code: String) : CheckSmsUIState()
    object Error : CheckSmsUIState()
    data class MessageError(val title: String, val message: String, val button: String) :
        CheckSmsUIState()
}