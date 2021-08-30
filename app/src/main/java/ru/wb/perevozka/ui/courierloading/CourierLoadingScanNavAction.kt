package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanNavAction {
    data class NavigateToUnknownBox(val title: String) : CourierLoadingScanNavAction()

    object NavigateToBoxes : CourierLoadingScanNavAction()

    object NavigateToHandle : CourierLoadingScanNavAction()

    object NavigateToFlightDeliveries : CourierLoadingScanNavAction()

    object NavigateToBack : CourierLoadingScanNavAction()

}