package ru.wb.perevozka.ui.courierwarehouses

sealed class CourierWarehousesUIState {

    data class ReceptionBoxesItem(val items: MutableList<CourierWarehousesItem>) :
        CourierWarehousesUIState()

    data class ReceptionBoxItem(val index: Int, val item: CourierWarehousesItem) :
        CourierWarehousesUIState()

    data class Empty(val info: String) : CourierWarehousesUIState()

}