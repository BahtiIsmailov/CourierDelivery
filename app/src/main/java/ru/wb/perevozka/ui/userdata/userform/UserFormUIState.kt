package ru.wb.perevozka.ui.userdata.userform

sealed class UserFormUIState {
    data class Error(val message: String, val type: UserFormQueryType) : UserFormUIState()
    data class ErrorFocus(val message: String, val type: UserFormQueryType) : UserFormUIState()
    data class Complete(val format: String, val type: UserFormQueryType) : UserFormUIState()
    object Next : UserFormUIState()
}