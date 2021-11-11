package ru.wb.go.ui.dcunloading

sealed class DcUnloadingBoxNotBelongState {
    data class BelongInfo(val toolbarTitle: String) : DcUnloadingBoxNotBelongState()
}