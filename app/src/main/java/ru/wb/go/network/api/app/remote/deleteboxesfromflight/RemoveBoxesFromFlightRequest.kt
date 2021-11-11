package ru.wb.go.network.api.app.remote.deleteboxesfromflight

data class RemoveBoxesFromFlightRequest(
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: DeleteBoxesCurrentOfficeRemote,
    val barcodes: List<String>,
)