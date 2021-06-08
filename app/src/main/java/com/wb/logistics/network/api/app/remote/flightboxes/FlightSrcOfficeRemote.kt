package com.wb.logistics.network.api.app.remote.flightboxes

data class FlightSrcOfficeRemote(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)