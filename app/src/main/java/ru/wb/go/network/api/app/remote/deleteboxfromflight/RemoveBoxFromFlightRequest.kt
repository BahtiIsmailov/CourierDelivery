package ru.wb.go.network.api.app.remote.deleteboxfromflight

data class RemoveBoxFromFlightRequest(
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: DeleteBoxCurrentOfficeRemote,
)