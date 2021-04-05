package com.wb.logistics.network.api.app.response.boxinfo

data class FlightRemote(
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)