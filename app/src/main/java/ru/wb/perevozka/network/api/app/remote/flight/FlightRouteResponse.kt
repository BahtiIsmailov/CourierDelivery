package ru.wb.perevozka.network.api.app.remote.flight

data class FlightRouteResponse(
    val id: Int,
    val changed: Boolean,
    val name: String
)