package com.wb.logistics.network.api.app.remote.flight

data class FlightRemote(
    val id: Int,
    val gate: Int,
    val dc: DcRemote,
    val offices: List<OfficeRemote>,
    val driver: DriverRemote,
    val route: RouteRemote?,
    val car: CarRemote,
    val plannedDate: String,
    val startedDate: String?,
    val status: String,
    val location: LocationRemote?,
)

