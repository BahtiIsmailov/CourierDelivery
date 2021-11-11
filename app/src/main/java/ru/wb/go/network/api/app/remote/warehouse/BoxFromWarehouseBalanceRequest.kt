package ru.wb.go.network.api.app.remote.warehouse

data class BoxFromWarehouseBalanceRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxFromWarehouseBalanceCurrentOfficeRequest,
)