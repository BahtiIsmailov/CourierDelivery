package ru.wb.perevozka.network.api.app.remote.pvz

data class BoxFromPvzBalanceDstOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)