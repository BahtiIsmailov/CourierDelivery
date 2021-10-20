package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanNavAction {

    object NavigateToUnknownBox : CourierLoadingScanNavAction()

    object NavigateToBoxes : CourierLoadingScanNavAction()

    object NavigateToConfirmDialog : CourierLoadingScanNavAction()

    object NavigateToBack : CourierLoadingScanNavAction()

    object NavigateToWarehouse : CourierLoadingScanNavAction()

    data class NavigateToIntransit(val amount: Int, val count: Int) : CourierLoadingScanNavAction()

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierLoadingScanNavAction()

}