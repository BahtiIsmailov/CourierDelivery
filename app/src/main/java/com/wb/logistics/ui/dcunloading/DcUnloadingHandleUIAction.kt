package com.wb.logistics.ui.dcunloading

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class DcUnloadingHandleUIAction {
    data class BoxChanges(val observable: InitialValueObservable<CharSequence>) : DcUnloadingHandleUIAction()
}