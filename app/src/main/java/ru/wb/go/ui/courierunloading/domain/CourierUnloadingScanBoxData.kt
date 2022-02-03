package ru.wb.go.ui.courierunloading.domain

sealed class CourierUnloadingScanBoxData {

    data class BoxAdded(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()

    data class WrongBox(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()
    data class ForbiddenBox(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()
    data class ScannerReady(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()

    object Empty : CourierUnloadingScanBoxData()

    object UnknownQr : CourierUnloadingScanBoxData()


    data class UnloadingCompleted(val qrCode: String, val address: String) :
        CourierUnloadingScanBoxData()

}