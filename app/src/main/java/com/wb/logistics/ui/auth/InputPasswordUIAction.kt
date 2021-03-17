package com.wb.logistics.ui.auth

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class InputPasswordUIAction {
    data class PasswordChanges(val observable: InitialValueObservable<CharSequence>) :
        InputPasswordUIAction()

    object RemindPassword : InputPasswordUIAction()
    data class Auth(val password: String) : InputPasswordUIAction()
}