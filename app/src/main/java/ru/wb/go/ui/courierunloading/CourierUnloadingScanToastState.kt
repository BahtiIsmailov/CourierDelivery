package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanToastState {

    data class BoxAdded(val message: String) : CourierUnloadingScanToastState()
    data class BoxHasBeenAdded(val message: String) : CourierUnloadingScanToastState()

}