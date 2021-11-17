package ru.wb.go.network.api.app.entity

data class CourierTasksMySrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)