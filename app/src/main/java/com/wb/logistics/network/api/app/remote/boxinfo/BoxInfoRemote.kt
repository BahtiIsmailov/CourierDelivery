package com.wb.logistics.network.api.app.remote.boxinfo

data class BoxInfoRemote(
    val box: BoxInfoItemRemote?,
    val flight: BoxInfoFlightRemote?,
)