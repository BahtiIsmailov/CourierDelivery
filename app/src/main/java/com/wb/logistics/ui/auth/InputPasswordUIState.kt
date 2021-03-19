package com.wb.logistics.ui.auth

sealed class InputPasswordUIState<out R> {

    data class NavigateToTemporaryPassword(val phone : String) : InputPasswordUIState<Nothing>()
    object NavigateToApplication : InputPasswordUIState<Nothing>()

    object NextEnable : InputPasswordUIState<Nothing>()
    object NextDisable : InputPasswordUIState<Nothing>()
    object Empty : InputPasswordUIState<Nothing>()

    object AuthProcess : InputPasswordUIState<Nothing>()
    object AuthComplete : InputPasswordUIState<Nothing>()
    data class Error(val message: String) : InputPasswordUIState<Nothing>()
}