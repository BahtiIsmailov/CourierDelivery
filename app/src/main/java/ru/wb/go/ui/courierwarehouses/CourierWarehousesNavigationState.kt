package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehousesNavigationState {

    data class NavigateToCourierOrder(val officeId: Int, val address: String) :
        CourierWarehousesNavigationState()

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierWarehousesNavigationState()

    object NavigateToBack : CourierWarehousesNavigationState()

}
