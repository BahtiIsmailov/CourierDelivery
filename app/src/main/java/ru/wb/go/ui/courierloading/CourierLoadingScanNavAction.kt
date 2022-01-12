package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanNavAction {

    object NavigateToConfirmDialog : CourierLoadingScanNavAction()

    object NavigateToWarehouse : CourierLoadingScanNavAction()

    data class NavigateToStartDelivery(val amount: Int, val count: Int) :
        CourierLoadingScanNavAction()

}