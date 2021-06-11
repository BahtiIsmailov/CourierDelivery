package com.wb.logistics.network.api.app.remote.boxinfo

data class BoxInfoFlightRemote(
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)