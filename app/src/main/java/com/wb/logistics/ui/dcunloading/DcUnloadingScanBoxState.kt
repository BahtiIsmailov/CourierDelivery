package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingScanBoxState {

    data class DcUnloadedBoxesNotBelong(val barcode: String) : DcUnloadingScanBoxState()

}