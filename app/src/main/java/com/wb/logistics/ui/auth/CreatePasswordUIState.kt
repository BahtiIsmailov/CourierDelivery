package com.wb.logistics.ui.auth

sealed class CreatePasswordUIState {

    object NavigateToApplication : CreatePasswordUIState()
    data class NavigateToTemporaryPassword(val phone: String) : CreatePasswordUIState()

    object SaveAndNextEnable : CreatePasswordUIState()
    object SaveAndNextDisable : CreatePasswordUIState()
    data class InitTitle(val title: String, val phone: String) : CreatePasswordUIState()

    object AuthProcess : CreatePasswordUIState()
    object AuthComplete : CreatePasswordUIState()
    data class Error(val message: String) : CreatePasswordUIState()
}