package com.wb.logistics.network.api.app.remote.flightboxtobalance

data class FlightBoxToBalanceRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: FlightBoxToBalanceCurrentOfficeRequest,
)