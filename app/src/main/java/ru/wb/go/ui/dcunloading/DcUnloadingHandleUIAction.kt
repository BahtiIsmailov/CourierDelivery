package ru.wb.go.ui.dcunloading

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class DcUnloadingHandleUIAction {
    data class BoxChanges(val observable: InitialValueObservable<CharSequence>) : DcUnloadingHandleUIAction()
}