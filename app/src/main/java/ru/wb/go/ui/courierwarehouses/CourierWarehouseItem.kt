package ru.wb.go.ui.courierwarehouses

data class CourierWarehouseItem(
    val id: Int,
    val name: String,
    val fullAddress: String,
    var isSelected: Boolean
)