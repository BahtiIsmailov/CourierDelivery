package ru.wb.perevozka.ui.auth

sealed class CheckSmsUIState {
    object SaveAndNextEnable : CheckSmsUIState()
    object SaveAndNextDisable : CheckSmsUIState()
    object Progress : CheckSmsUIState()
    object Complete : CheckSmsUIState()
    object Error : CheckSmsUIState()
    data class MessageError(val message: String) : CheckSmsUIState()
}