package ru.wb.go.network.api.app.remote.pvz

data class BoxFromPvzBalanceSrcOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)