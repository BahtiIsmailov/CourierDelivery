package ru.wb.go.ui.courierloading.domain

sealed class CourierLoadingScanBoxData {

    data class FirstBoxAdded(val qrCode: String, val address: String) : CourierLoadingScanBoxData()

    data class SecondaryBoxAdded(val qrCode: String, val address: String) : CourierLoadingScanBoxData()

    data class ForbiddenTakeBox(val qrCode: String) : CourierLoadingScanBoxData()

    object NotRecognizedQr : CourierLoadingScanBoxData()

}