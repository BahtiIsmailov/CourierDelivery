package ru.wb.perevozka.network.api.app.remote.courier

data class CourierOrderDstOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)