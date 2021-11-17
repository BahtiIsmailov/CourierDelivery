package ru.wb.go.network.api.app.remote.tracker

data class BoxTrackerRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxTrackerCurrentOfficeRequest,
    val flight: BoxTrackerFlightRequest,
    val event: String,
)