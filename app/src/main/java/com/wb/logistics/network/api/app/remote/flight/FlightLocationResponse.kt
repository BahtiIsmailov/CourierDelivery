package com.wb.logistics.network.api.app.remote.flight

data class FlightLocationResponse(
    val office: FlightOfficeLocationResponse?,
    val getFromGPS: Boolean?,
)

