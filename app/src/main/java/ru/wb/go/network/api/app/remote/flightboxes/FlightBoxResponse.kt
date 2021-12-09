package ru.wb.go.network.api.app.remote.flightboxes

data class FlightBoxResponse(
    val srcOffice: FlightSrcOfficeResponse,
    val dstOffice: FlightDstOfficeResponse,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
    val onBoard: Boolean,
)