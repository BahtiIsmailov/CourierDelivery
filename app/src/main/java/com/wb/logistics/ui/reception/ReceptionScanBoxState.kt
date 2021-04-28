package com.wb.logistics.ui.reception

sealed class ReceptionScanBoxState<out R> {

    object Empty : ReceptionScanBoxState<Nothing>()

    data class BoxAdded(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxState<Nothing>()

    data class BoxInit(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxState<Nothing>()

    data class BoxDeny(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxState<Nothing>()

    data class BoxHasBeenAdded(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxState<Nothing>()

}