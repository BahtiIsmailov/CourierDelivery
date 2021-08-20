package ru.wb.perevozka.ui.courierwarehouses

sealed class CourierWarehousesUINavState {

    data class NavigateToCourierOrder(val officeId: Int, val address: String) : CourierWarehousesUINavState()

    data class NavigateToMessageInfo(val message: String, val button: String) : CourierWarehousesUINavState()

    object NavigateToBack : CourierWarehousesUINavState()

}
