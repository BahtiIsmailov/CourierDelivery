package ru.wb.go.network.api.app.remote.flightsstatus

data class StatusLocationResponse(
    val office: StatusOfficeLocationResponse,
    val getFromGPS: Boolean,
)

