package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanNavAction {
    object NavigateToUnknownBox : CourierLoadingScanNavAction()

    object NavigateToBoxes : CourierLoadingScanNavAction()

    object NavigateToHandle : CourierLoadingScanNavAction()

    object NavigateToFlightDeliveries : CourierLoadingScanNavAction()

    object NavigateToBack : CourierLoadingScanNavAction()

}