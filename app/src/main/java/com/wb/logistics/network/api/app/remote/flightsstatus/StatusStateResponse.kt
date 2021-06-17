package com.wb.logistics.network.api.app.remote.flightsstatus

data class StatusStateResponse(
    val status: String,
    val location: StatusLocationResponse,
)