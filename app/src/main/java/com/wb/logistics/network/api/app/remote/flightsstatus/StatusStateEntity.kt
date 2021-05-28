package com.wb.logistics.network.api.app.remote.flightsstatus

data class StatusStateEntity(
    val status: String,
    val location: StatusLocationEntity,
)