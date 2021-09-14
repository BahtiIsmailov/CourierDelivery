package ru.wb.perevozka.ui.courierdelivery

data class CourierDeliveryItem(
    val id: Int,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
    val isSelected: Boolean
)