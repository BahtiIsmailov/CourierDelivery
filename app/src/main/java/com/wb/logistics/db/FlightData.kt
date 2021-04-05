package com.wb.logistics.db

data class FlightData(
    val flight: Int,
    val parkingNumber: Int,
    val date: String,
    val routesTitle: String,
    val offices: List<String>
)

