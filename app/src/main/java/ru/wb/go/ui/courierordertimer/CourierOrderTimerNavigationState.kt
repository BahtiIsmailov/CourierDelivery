package ru.wb.go.ui.courierordertimer

sealed class CourierOrderTimerNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierOrderTimerNavigationState()

    data class NavigateToRefuseOrderDialog(
        val title: String, val message: String
    ) : CourierOrderTimerNavigationState()

    object NavigateToWarehouse: CourierOrderTimerNavigationState()

    object NavigateToScanner: CourierOrderTimerNavigationState()


}
