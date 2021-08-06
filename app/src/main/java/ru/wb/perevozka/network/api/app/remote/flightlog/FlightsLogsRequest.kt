package ru.wb.perevozka.network.api.app.remote.flightlog

data class FlightsLogsRequest(
    val flightsLogs: List<FlightLogRequest>
)