package ru.wb.go.ui.courierorders

sealed class CourierOrderAddressesUIState {

    data class InitItems(val items: MutableList<CourierOrderDetailsAddressItem>) :
        CourierOrderAddressesUIState()

    data class UpdateItems(val index: Int, val items: MutableList<CourierOrderDetailsAddressItem>) :
        CourierOrderAddressesUIState()

    object Empty : CourierOrderAddressesUIState()

}