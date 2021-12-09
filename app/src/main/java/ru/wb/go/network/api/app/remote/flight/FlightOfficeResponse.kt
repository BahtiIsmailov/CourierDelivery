package ru.wb.go.network.api.app.remote.flight

data class FlightOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
    val visitedAt: String?
)
