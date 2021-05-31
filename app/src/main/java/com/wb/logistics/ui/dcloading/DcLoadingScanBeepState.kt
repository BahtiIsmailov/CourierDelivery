package com.wb.logistics.ui.dcloading

sealed class DcLoadingScanBeepState {

    object BoxAdded : DcLoadingScanBeepState()
    object BoxSkipAdded : DcLoadingScanBeepState()

}