package ru.wb.go.network.api.app.remote.boxinfo

data class BoxInfoResponse(
    val box: BoxInfoItemResponse?,
    val flight: BoxInfoFlightResponse?,
)