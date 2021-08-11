package ru.wb.perevozka.ui.userdata.userform

sealed class UserFormUIRepeatState {
    object RepeatPasswordProgress : UserFormUIRepeatState()
    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        UserFormUIRepeatState()

    object RepeatPassword : UserFormUIRepeatState()
    data class ErrorPassword(val message: String) : UserFormUIRepeatState()
}