package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanBoxState {

    object Empty : CourierLoadingScanBoxState()

    data class BoxInit(val qrCode: String, val address: String, val accepted: String) :
        CourierLoadingScanBoxState()

    data class BoxAdded(val qrCode: String, val address: String, val accepted: String) :
        CourierLoadingScanBoxState()

    object UnknownBoxTimer : CourierLoadingScanBoxState()

    data class UnknownBox(val qrCode: String, val address: String, val accepted: String) :
        CourierLoadingScanBoxState()

}