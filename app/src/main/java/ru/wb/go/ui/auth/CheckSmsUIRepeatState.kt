package ru.wb.go.ui.auth

sealed class CheckSmsUIRepeatState {
    object RepeatPasswordProgress : CheckSmsUIRepeatState()
    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        CheckSmsUIRepeatState()

    object RepeatPasswordComplete : CheckSmsUIRepeatState()
    data class ErrorPassword(val title: String, val message: String, val button: String) : CheckSmsUIRepeatState()
}