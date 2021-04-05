package com.wb.logistics.network.api.app.response.boxinfo

data class BoxInfoRemote(
    val box: BoxRemote,
    val flight: FlightRemote,
)