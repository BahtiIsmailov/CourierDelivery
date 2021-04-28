package com.wb.logistics.network.api.app.remote

data class PutBoxFromFlightRemote(
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: PutBoxCurrentOfficeRemote,
)