package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanBeepState {

    object BoxAdded : CourierUnloadingScanBeepState()
    object UnknownBox : CourierUnloadingScanBeepState()

}