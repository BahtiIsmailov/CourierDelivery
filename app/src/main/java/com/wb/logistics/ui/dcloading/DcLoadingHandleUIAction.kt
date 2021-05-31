package com.wb.logistics.ui.dcloading

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class DcLoadingHandleUIAction {
    data class BoxChanges(val observable: InitialValueObservable<CharSequence>) : DcLoadingHandleUIAction()
}