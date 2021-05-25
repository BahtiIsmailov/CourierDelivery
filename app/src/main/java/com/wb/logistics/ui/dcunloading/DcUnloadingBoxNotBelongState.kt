package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingBoxNotBelongState {
    data class BelongInfo(val toolbarTitle: String) : DcUnloadingBoxNotBelongState()
}