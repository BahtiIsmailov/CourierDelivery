package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanNavAction {

    object NavigateToUnknownBox : CourierUnloadingScanNavAction()

    object NavigateToBoxes : CourierUnloadingScanNavAction()

    object NavigateToIntransit : CourierUnloadingScanNavAction()

}