package ru.wb.perevozka.network.api.app.entity

data class CourierTasksMySrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)