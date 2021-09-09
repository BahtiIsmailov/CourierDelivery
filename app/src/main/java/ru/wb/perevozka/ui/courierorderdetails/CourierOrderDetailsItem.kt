package ru.wb.perevozka.ui.courierorderdetails

data class CourierOrderDetailsItem(
    val id: Int,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
    val isSelected: Boolean
)