package com.wb.logistics.ui.reception

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class ReceptionHandleUIAction {
    data class BoxChanges(val observable: InitialValueObservable<CharSequence>) : ReceptionHandleUIAction()
}