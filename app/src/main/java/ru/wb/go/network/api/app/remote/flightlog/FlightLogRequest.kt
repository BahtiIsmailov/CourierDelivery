package ru.wb.go.network.api.app.remote.flightlog

data class FlightLogRequest(
    val id: Int,
    val createdAt: String,
    val data: String,
)