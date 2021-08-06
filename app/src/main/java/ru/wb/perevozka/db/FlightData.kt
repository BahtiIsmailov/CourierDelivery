package ru.wb.perevozka.db

data class FlightData(
    val flightId: Int,
    val gate: Int,
    val date: String,
    val routesTitle: String,
    val offices: List<String>
)

