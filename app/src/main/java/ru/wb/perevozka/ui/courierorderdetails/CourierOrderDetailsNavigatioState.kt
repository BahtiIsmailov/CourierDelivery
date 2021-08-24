package ru.wb.perevozka.ui.courierorderdetails

sealed class CourierOrderDetailsNavigatioState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierOrderDetailsNavigatioState()

    data class NavigateToDialogConfirm(
        val title: String, val message: String
    ) : CourierOrderDetailsNavigatioState()

}
