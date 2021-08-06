package ru.wb.perevozka.network.api.app.remote.warehouse

data class BoxFromWarehouseBalanceDstOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)