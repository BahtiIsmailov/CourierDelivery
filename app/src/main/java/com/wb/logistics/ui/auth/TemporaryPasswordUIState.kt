package com.wb.logistics.ui.auth

sealed class TemporaryPasswordUIState {
    object FetchingTmpPassword : TemporaryPasswordUIState()

    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        TemporaryPasswordUIState()

    object RepeatPassword : TemporaryPasswordUIState()

    object Progress : TemporaryPasswordUIState()
    object NextEnable : TemporaryPasswordUIState()
    object NextDisable : TemporaryPasswordUIState()

    data class Update(val message: String) : TemporaryPasswordUIState()
    object Error : TemporaryPasswordUIState()
}