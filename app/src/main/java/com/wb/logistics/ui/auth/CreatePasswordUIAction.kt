package com.wb.logistics.ui.auth

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class CreatePasswordUIAction {
    data class PasswordChanges(val observable: InitialValueObservable<CharSequence>) :
        CreatePasswordUIAction()

    data class Auth(val password: String) : CreatePasswordUIAction()
}