package com.wb.logistics.network.api.app.remote.flight

data class FlightResponse(
    val id: Int?,
    val gate: Int?,
    val dc: FlightDcResponse?,
    val offices: List<FlightOfficeResponse>?,
    val driver: FlightDriverResponse?,
    val route: FlightRouteResponse?,
    val car: FlightCarResponse?,
    val plannedDate: String?,
    val startedDate: String?,
    val status: String?,
    val location: FlightLocationResponse?,
)

