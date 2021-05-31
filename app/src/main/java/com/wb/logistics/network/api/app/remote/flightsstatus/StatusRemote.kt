package com.wb.logistics.network.api.app.remote.flightsstatus

data class StatusRemote(
    val status: String,
    val location: StatusLocationRemote,
    val updatedAt: String
)