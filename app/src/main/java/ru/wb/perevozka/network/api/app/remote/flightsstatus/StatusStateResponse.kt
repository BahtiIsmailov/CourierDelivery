package ru.wb.perevozka.network.api.app.remote.flightsstatus

data class StatusStateResponse(
    val status: String,
    val location: StatusLocationResponse,
)