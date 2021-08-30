package ru.wb.perevozka.ui.courierloading.domain

sealed class CourierLoadingScanBoxData {

    data class BoxAdded(val qrCode: String, val address: String) : CourierLoadingScanBoxData()

    object UnknownBox : CourierLoadingScanBoxData()

    object Empty : CourierLoadingScanBoxData()

}