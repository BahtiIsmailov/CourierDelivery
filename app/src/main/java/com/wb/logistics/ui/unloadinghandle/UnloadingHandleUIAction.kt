package com.wb.logistics.ui.unloadinghandle

import com.jakewharton.rxbinding3.InitialValueObservable

sealed class UnloadingHandleUIAction {
    data class BoxChanges(val observable: InitialValueObservable<CharSequence>) : UnloadingHandleUIAction()
}