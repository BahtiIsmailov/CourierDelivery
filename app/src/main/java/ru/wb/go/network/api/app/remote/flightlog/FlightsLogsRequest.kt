package ru.wb.go.network.api.app.remote.flightlog

data class FlightsLogsRequest(
    val flightsLogs: List<FlightLogRequest>
)