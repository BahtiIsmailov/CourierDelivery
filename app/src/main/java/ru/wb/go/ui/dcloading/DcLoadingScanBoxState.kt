package ru.wb.go.ui.dcloading

sealed class DcLoadingScanBoxState {

    object Empty : DcLoadingScanBoxState()

    data class BoxAdded(val accepted: String, val gate: String, val barcode: String) :
        DcLoadingScanBoxState()

    data class BoxInit(val accepted: String, val gate: String, val barcode: String) :
        DcLoadingScanBoxState()

    data class BoxDeny(val accepted: String, val gate: String, val barcode: String) :
        DcLoadingScanBoxState()

    data class BoxHasBeenAdded(val accepted: String, val gate: String, val barcode: String) :
        DcLoadingScanBoxState()

}