package ru.wb.go.network.api.app.entity

data class CourierTaskMyDstOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)