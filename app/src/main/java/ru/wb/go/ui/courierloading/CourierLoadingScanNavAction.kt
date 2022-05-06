package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanNavAction {

    object NavigateToConfirmDialog : CourierLoadingScanNavAction()

    object NavigateToWarehouse : CourierLoadingScanNavAction()

    data class NavigateToStartDelivery(val amount: Int, val count: Int) :
        CourierLoadingScanNavAction()

    data class InitAndShowLoadingItems(
        val pvzCount: String,
        val boxCount: String,
        val items: MutableList<CourierLoadingDetailsItem>
    ) : CourierLoadingScanNavAction()

    object HideLoadingItems : CourierLoadingScanNavAction()

}