package ru.wb.perevozka.network.api.app.remote.pvz

data class BoxFromPvzBalanceRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxFromPvzBalanceCurrentOfficeRequest,
)