package ru.wb.perevozka.db.entity.courier

data class CourierOrderSrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)