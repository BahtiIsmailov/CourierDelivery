package ru.wb.go.network.api.app.remote.flight

data class FlightLocationResponse(
    val office: FlightOfficeLocationResponse?,
    val getFromGPS: Boolean?,
)

