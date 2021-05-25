package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingScanBoxState {

    data class DcUnloadedBoxesEmpty(val accepted: String) : DcUnloadingScanBoxState()

    data class DcUnloadedBoxesComplete(val accepted: String, val barcode: String) : DcUnloadingScanBoxState()

}