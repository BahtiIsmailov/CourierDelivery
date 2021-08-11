package ru.wb.perevozka.ui.userdata.userform

import android.util.Pair

sealed class UserFormUIState {

    object Progress : UserFormUIState()
    object NextEnable : UserFormUIState()
    object NextDisable : UserFormUIState()

    data class Update(val message: String) : UserFormUIState()

    data class PasswordNotFound(val message: String) : UserFormUIState()
    data class Error(val message: String) : UserFormUIState()

    data class ChangeDataField(val data: Pair<String, UserFormQueryType>) : UserFormUIState()
}