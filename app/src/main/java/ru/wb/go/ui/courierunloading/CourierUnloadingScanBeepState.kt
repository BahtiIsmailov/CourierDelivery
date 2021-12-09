package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanBeepState {

    object BoxAdded : CourierUnloadingScanBeepState()
    object UnknownBox : CourierUnloadingScanBeepState()

}