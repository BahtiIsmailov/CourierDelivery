package com.wb.logistics.network.api.app.remote.deleteboxfromflight

data class DeleteBoxFromFlightRemote(
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: DeleteBoxCurrentOfficeRemote,
)