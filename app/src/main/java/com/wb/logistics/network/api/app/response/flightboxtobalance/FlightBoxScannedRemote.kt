package com.wb.logistics.network.api.app.response.flightboxtobalance

data class FlightBoxScannedRemote(
    val barcode: String,
    val isManualInput: Boolean,
    val dstOffice: CurrentOfficeRemote,
)