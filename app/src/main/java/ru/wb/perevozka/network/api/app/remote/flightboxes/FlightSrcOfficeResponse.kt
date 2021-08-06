package ru.wb.perevozka.network.api.app.remote.flightboxes

data class FlightSrcOfficeResponse(
    val id: Int?,
    val name: String?,
    val fullAddress: String?,
    val long: Double?,
    val lat: Double?,
)