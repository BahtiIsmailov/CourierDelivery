package ru.wb.perevozka.ui.courierorderconferm

sealed class CourierOrderConfirmNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierOrderConfirmNavigationState()

    data class NavigateToRefuseOrderDialog(
        val title: String, val message: String
    ) : CourierOrderConfirmNavigationState()

    object NavigateToWarehouse: CourierOrderConfirmNavigationState()

    object NavigateToTimer: CourierOrderConfirmNavigationState()


}
