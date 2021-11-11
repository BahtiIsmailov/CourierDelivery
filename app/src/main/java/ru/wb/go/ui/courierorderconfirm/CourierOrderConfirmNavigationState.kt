package ru.wb.go.ui.courierorderconfirm

sealed class CourierOrderConfirmNavigationState {

    data class NavigateToRefuseOrderDialog(
        val title: String, val message: String
    ) : CourierOrderConfirmNavigationState()

    object NavigateToBack: CourierOrderConfirmNavigationState()

    object NavigateToTimer: CourierOrderConfirmNavigationState()

    object NavigateToChangeCar: CourierOrderConfirmNavigationState()


}
