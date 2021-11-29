package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanNavAction {

    object NavigateToUnknownBox : CourierUnloadingScanNavAction()

    object NavigateToBoxes : CourierUnloadingScanNavAction()

    object NavigateToIntransit : CourierUnloadingScanNavAction()

}