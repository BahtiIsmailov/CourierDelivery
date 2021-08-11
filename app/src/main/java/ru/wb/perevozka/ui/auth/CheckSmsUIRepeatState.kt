package ru.wb.perevozka.ui.auth

sealed class CheckSmsUIRepeatState {
    object RepeatPasswordProgress : CheckSmsUIRepeatState()
    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        CheckSmsUIRepeatState()

    object RepeatPasswordComplete : CheckSmsUIRepeatState()
    data class ErrorPassword(val message: String) : CheckSmsUIRepeatState()
}