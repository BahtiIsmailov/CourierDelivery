package com.wb.logistics.ui.auth

sealed class TemporaryPasswordUIRepeatState {
    object RepeatPasswordProgress : TemporaryPasswordUIRepeatState()
    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        TemporaryPasswordUIRepeatState()

    object RepeatPassword : TemporaryPasswordUIRepeatState()
    data class ErrorPassword(val message: String) : TemporaryPasswordUIRepeatState()
}