package com.wb.logistics.network.api.app.remote.deleteboxesfromflight

data class DeleteBoxesFromFlightRemote(
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: DeleteBoxesCurrentOfficeRemote,
    val barcodes: List<String>,
)