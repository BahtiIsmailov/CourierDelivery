package com.wb.logistics.ui.auth

sealed class TemporaryPasswordUIState<out R> {
    object FetchingTmpPassword : TemporaryPasswordUIState<Nothing>()
    data class RemainingAttempts(val remainingAttempts: String) :
        TemporaryPasswordUIState<Nothing>()

    data class NavigateToCreatePassword(val phone: String) : TemporaryPasswordUIState<Nothing>()

    data class InitTitle(val title: String, val phone: String) : TemporaryPasswordUIState<Nothing>()
    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        TemporaryPasswordUIState<Nothing>()

    object RepeatPassword : TemporaryPasswordUIState<Nothing>()

    object NextEnable : TemporaryPasswordUIState<Nothing>()
    object NextDisable : TemporaryPasswordUIState<Nothing>()

    data class Update(val message: String) : TemporaryPasswordUIState<Nothing>()
    data class Error(val message: String) : TemporaryPasswordUIState<Nothing>()
}