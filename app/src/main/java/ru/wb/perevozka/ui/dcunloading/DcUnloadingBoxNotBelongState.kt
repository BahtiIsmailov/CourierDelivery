package ru.wb.perevozka.ui.dcunloading

sealed class DcUnloadingBoxNotBelongState {
    data class BelongInfo(val toolbarTitle: String) : DcUnloadingBoxNotBelongState()
}