package com.wb.logistics.ui.reception

sealed class ReceptionBoxUIState<out R> {

    object Empty : ReceptionBoxUIState<Nothing>()

    data class BoxComplete(
        val toastBox: String,
        val accepted: String,
        val gate: String,
        val barcode: String,
    ) :
        ReceptionBoxUIState<Nothing>()

    data class BoxInit(
        val accepted: String,
        val gate: String,
        val barcode: String,
    ) :
        ReceptionBoxUIState<Nothing>()

    data class BoxDeny(val accepted: String, val gate: String, val barcode: String) :
        ReceptionBoxUIState<Nothing>()

    data class BoxHasBeenAdded(
        val toastBox: String,
        val accepted: String,
        val gate: String,
        val barcode: String,
    ) :
        ReceptionBoxUIState<Nothing>()

}