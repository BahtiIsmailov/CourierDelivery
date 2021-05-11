package com.wb.logistics.ui.auth

sealed class InputPasswordUIState {

    data class NavigateToTemporaryPassword(val phone : String) : InputPasswordUIState()
    object NavigateToApplication : InputPasswordUIState()

    object NextEnable : InputPasswordUIState()
    object NextDisable : InputPasswordUIState()
    object Empty : InputPasswordUIState()

    object AuthProcess : InputPasswordUIState()
    object AuthComplete : InputPasswordUIState()
    data class Error(val message: String) : InputPasswordUIState()
}