package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehousesNavigationState {

    data class NavigateToCourierOrders(
        val officeId: Int,
        val warehouseLatitude: Double,
        val warehouseLongitude: Double,
        val address: String
    ) : CourierWarehousesNavigationState()

    object NavigateToBack : CourierWarehousesNavigationState()

    object NavigateToRegistration : CourierWarehousesNavigationState()

}
