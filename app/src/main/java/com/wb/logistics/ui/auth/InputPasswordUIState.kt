package com.wb.logistics.ui.auth

sealed class InputPasswordUIState {

    object NextEnable : InputPasswordUIState()
    object NextDisable : InputPasswordUIState()

    object AuthProcess : InputPasswordUIState()
    object AuthComplete : InputPasswordUIState()

    data class PasswordNotFound(val message: String) : InputPasswordUIState()
    data class Error(val message: String) : InputPasswordUIState()

}