package ru.wb.perevozka.ui.courierwarehouses

sealed class CourierWarehousesUIState {

    data class InitItems(val items: MutableList<CourierWarehousesItem>) :
        CourierWarehousesUIState()

    data class UpdateItems(val index: Int, val item: CourierWarehousesItem) :
        CourierWarehousesUIState()

    data class Empty(val info: String) : CourierWarehousesUIState()

}