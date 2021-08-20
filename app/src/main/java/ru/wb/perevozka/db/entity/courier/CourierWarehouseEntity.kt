package ru.wb.perevozka.db.entity.courier

data class CourierWarehouseEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)