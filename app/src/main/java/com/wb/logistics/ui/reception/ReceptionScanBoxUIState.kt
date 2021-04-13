package com.wb.logistics.ui.reception

sealed class ReceptionScanBoxUIState<out R> {

    object Empty : ReceptionScanBoxUIState<Nothing>()

    data class BoxAdded(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxUIState<Nothing>()

    data class BoxInit(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxUIState<Nothing>()

    data class BoxDeny(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxUIState<Nothing>()

    data class BoxHasBeenAdded(val accepted: String, val gate: String, val barcode: String) :
        ReceptionScanBoxUIState<Nothing>()

}