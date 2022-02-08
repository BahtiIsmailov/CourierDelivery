package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanNavAction {

    data class
    NavigateToBoxes(val officeId: Int) : CourierUnloadingScanNavAction()

    object NavigateToIntransit : CourierUnloadingScanNavAction()

}