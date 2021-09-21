package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanBoxState {

    data class Empty(
        val status: String,
        val qrCode: String,
        val address: String,
        val accepted: String
    ) :
        CourierUnloadingScanBoxState()

    data class BoxInit(
        val status: String,
        val qrCode: String,
        val address: String,
        val accepted: String
    ) :
        CourierUnloadingScanBoxState()

    data class BoxAdded(
        val status: String,
        val qrCode: String,
        val address: String,
        val accepted: String
    ) :
        CourierUnloadingScanBoxState()

    data class UnknownBox(
        val status: String,
        val qrCode: String,
        val address: String,
        val accepted: String
    ) :
        CourierUnloadingScanBoxState()

    data class ScannerReady(
        val status: String,
        val qrCode: String,
        val address: String,
        val accepted: String
    ) :
        CourierUnloadingScanBoxState()

}