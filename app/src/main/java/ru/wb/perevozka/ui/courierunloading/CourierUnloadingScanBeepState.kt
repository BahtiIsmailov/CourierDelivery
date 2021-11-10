package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanBeepState {

    object BoxAdded : CourierUnloadingScanBeepState()
    object UnknownQR : CourierUnloadingScanBeepState()
    object UnknownBox : CourierUnloadingScanBeepState()

}