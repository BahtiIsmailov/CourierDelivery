package com.wb.logistics.network.api.app.remote.flight

data class FlightRouteResponse(
    val id: Int,
    val changed: Boolean,
    val name: String
)