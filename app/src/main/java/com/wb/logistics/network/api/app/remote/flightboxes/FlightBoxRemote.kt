package com.wb.logistics.network.api.app.remote.flightboxes

data class FlightBoxRemote(
    val srcOffice: FlightSrcOfficeRemote,
    val dstOffice: FlightDstOfficeRemote,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
    val onBoard: Boolean,
)