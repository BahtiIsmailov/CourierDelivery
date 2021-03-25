package com.wb.logistics.ui.flights.domain

data class FlightsData(
    val flight: Int,
    val parkingNumber: Int,
    val date: String,
    val routesTitle: String,
    val routes: List<String>
)

