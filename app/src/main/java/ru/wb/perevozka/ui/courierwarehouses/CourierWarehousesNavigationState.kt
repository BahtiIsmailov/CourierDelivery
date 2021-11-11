package ru.wb.perevozka.ui.courierwarehouses

sealed class CourierWarehousesNavigationState {

    data class NavigateToCourierOrder(val officeId: Int, val address: String) :
        CourierWarehousesNavigationState()

    object NavigateToBack : CourierWarehousesNavigationState()

}
