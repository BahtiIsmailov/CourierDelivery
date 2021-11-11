package ru.wb.go.network.api.app.remote.flightsstatus

data class StatusResponse(
    val status: String,
    val location: StatusLocationResponse,
    val updatedAt: String
)