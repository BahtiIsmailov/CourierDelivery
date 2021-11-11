package ru.wb.go.network.api.app.remote.flightsstatus

data class StatusLocationEntity(
    val office: StatusOfficeLocationEntity,
    val getFromGPS: Boolean?,
)

