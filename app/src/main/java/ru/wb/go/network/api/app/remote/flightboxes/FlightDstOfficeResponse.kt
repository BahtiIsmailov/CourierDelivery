package ru.wb.go.network.api.app.remote.flightboxes

data class FlightDstOfficeResponse(
    val id: Int?,
    val name: String?,
    val fullAddress: String?,
    val long: Double?,
    val lat: Double?,
)