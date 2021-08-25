package ru.wb.perevozka.ui.courierorderdetails

sealed class CourierOrderDetailsNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierOrderDetailsNavigationState()

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierOrderDetailsNavigationState()

    object NavigateToCarNumber: CourierOrderDetailsNavigationState()

}
