package com.wb.logistics.ui.auth

sealed class CreatePasswordUIState<out R> {

    object NavigateToApplication : CreatePasswordUIState<Nothing>()

    object SaveAndNextEnable : CreatePasswordUIState<Nothing>()
    object SaveAndNextDisable : CreatePasswordUIState<Nothing>()
    data class InitTitle(val title: String, val phone: String) : CreatePasswordUIState<Nothing>()

    object AuthProcess : CreatePasswordUIState<Nothing>()
    object AuthComplete : CreatePasswordUIState<Nothing>()
    data class Error(val message: String) : CreatePasswordUIState<Nothing>()
}