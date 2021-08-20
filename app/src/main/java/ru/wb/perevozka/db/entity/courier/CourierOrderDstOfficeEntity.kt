package ru.wb.perevozka.db.entity.courier

data class CourierOrderDstOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)