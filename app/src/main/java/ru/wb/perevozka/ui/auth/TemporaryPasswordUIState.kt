package ru.wb.perevozka.ui.auth

sealed class TemporaryPasswordUIState {

    object Progress : TemporaryPasswordUIState()
    object NextEnable : TemporaryPasswordUIState()
    object NextDisable : TemporaryPasswordUIState()

    data class Update(val message: String) : TemporaryPasswordUIState()

    data class PasswordNotFound(val message: String) : TemporaryPasswordUIState()
    data class Error(val message: String) : TemporaryPasswordUIState()
}