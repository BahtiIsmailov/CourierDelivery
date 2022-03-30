package ru.wb.go.ui.courierorderdetails

sealed class CourierOrderAddressesUIState {

    data class InitItems(val items: MutableList<CourierOrderDetailsItem>) :
        CourierOrderAddressesUIState()

    data class UpdateItems(val index: Int, val items: MutableList<CourierOrderDetailsItem>) :
        CourierOrderAddressesUIState()

    object Empty : CourierOrderAddressesUIState()

}