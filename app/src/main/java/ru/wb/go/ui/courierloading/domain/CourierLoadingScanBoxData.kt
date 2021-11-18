package ru.wb.go.ui.courierloading.domain

sealed class CourierLoadingScanBoxData {

    data class BoxFirstAdded(val qrCode: String, val address: String) : CourierLoadingScanBoxData()

    data class BoxAdded(val qrCode: String, val address: String) : CourierLoadingScanBoxData()

    data class UnknownBox(val qrCode: String) : CourierLoadingScanBoxData()

    data class UnknownQr(val qrCode: String) : CourierLoadingScanBoxData()

    object Empty : CourierLoadingScanBoxData()

}