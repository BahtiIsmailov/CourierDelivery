package ru.wb.go.ui.courierunloading.domain

sealed class CourierUnloadingScanBoxData {

    data class BoxAdded(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()

    data class UnknownBox(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()

    data class ScannerReady(val qrCode: String, val address: String) : CourierUnloadingScanBoxData()

    object Empty : CourierUnloadingScanBoxData()

    data class UnloadingCompleted(val qrCode: String, val address: String) :
        CourierUnloadingScanBoxData()

}