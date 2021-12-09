package ru.wb.go.network.api.app.remote.warehouse

data class BoxToWarehouseBalanceDstOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)