package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanNavAction {

    object NavigateToUnknownBox : CourierLoadingScanNavAction()

    object NavigateToBoxes : CourierLoadingScanNavAction()

    object NavigateToConfirmDialog : CourierLoadingScanNavAction()

    object NavigateToWarehouse : CourierLoadingScanNavAction()

    data class NavigateToIntransit(val amount: Int, val count: Int) : CourierLoadingScanNavAction()

}