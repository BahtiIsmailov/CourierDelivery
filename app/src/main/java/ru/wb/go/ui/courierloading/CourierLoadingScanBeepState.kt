package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanBeepState {

    object BoxFirstAdded : CourierLoadingScanBeepState()
    object BoxAdded : CourierLoadingScanBeepState()
    object UnknownQR : CourierLoadingScanBeepState()
    object UnknownBox : CourierLoadingScanBeepState()

}