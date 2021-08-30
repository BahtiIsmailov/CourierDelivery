package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanBeepState {

    object BoxAdded : CourierLoadingScanBeepState()
    object UnknownBox : CourierLoadingScanBeepState()

}