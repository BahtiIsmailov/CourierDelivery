package ru.wb.perevozka.network.api.app.remote.flightlog

data class FlightLogRequest(
    val id: Int,
    val createdAt: String,
    val data: String,
)