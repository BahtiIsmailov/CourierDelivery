package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingScanCounterBoxState {

    data class DcUnloadedBoxesEmpty(val accepted: String) : DcUnloadingScanCounterBoxState()

    data class DcUnloadedBoxesComplete(val accepted: String, val barcode: String) : DcUnloadingScanCounterBoxState()

}