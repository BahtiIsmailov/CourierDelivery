package ru.wb.perevozka.network.api.app.remote.flight

data class FlightOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
    val visitedAt: String?
)