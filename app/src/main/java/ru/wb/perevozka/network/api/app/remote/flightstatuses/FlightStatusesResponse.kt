package ru.wb.perevozka.network.api.app.remote.flightstatuses

data class FlightStatusesResponse(
    val data: List<FlightStatusRemote>
)