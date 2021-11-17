package ru.wb.go.network.api.app.remote.pvz

data class BoxToPvzBalanceRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxToPvzBalanceCurrentOfficeRequest,
)