package com.wb.logistics.ui.auth

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class NumberPhoneUIAction {
    object LongTitle : NumberPhoneUIAction()

    data class CheckPhone(val number: String) : NumberPhoneUIAction()
    data class NumberChanges(val observable: InitialValueObservable<CharSequence>) : NumberPhoneUIAction()
}