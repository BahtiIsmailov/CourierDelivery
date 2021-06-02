package com.wb.logistics.ui.auth

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class TemporaryPasswordUIAction{
    data class PasswordChanges(val observable: InitialValueObservable<CharSequence>) : TemporaryPasswordUIAction()
    data class CheckPassword(val password: String) : TemporaryPasswordUIAction()
}