package com.wb.logistics.network.api.app.remote.flightboxtobalance

data class FlightBoxScannedRemote(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: CurrentOfficeRemote,
)