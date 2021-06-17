package com.wb.logistics.network.api.app.remote.boxinfo

data class BoxInfoResponse(
    val box: BoxInfoItemResponse?,
    val flight: BoxInfoFlightResponse?,
)