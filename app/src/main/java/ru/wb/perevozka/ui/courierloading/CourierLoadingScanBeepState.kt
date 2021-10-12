package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanBeepState {

    object BoxFirstAdded : CourierLoadingScanBeepState()
    object BoxAdded : CourierLoadingScanBeepState()
    object UnknownBox : CourierLoadingScanBeepState()

}