package ru.wb.perevozka.network.api.app.remote.pvz

data class BoxToPvzBalanceRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxToPvzBalanceCurrentOfficeRequest,
)