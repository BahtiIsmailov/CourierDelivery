package ru.wb.perevozka.network.api.app.remote.flightsstatus

data class StatusStateEntity(
    val status: String,
    val location: StatusLocationEntity,
)