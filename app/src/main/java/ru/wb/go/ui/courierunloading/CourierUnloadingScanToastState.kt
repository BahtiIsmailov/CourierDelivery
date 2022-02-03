package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanToastState {

    data class BoxAdded(val message: String) : CourierUnloadingScanToastState()

}