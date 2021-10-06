package ru.wb.perevozka.ui.courierorderconfirm

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

    object NavigateToBack: CourierOrderConfirmNavigationState()

    object NavigateToTimer: CourierOrderConfirmNavigationState()

    object NavigateToChangeCar: CourierOrderConfirmNavigationState()


}
