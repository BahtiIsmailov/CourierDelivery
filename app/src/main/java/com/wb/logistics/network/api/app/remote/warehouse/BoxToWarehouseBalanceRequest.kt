package com.wb.logistics.network.api.app.remote.warehouse

data class BoxToWarehouseBalanceRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxToWarehouseBalanceCurrentOfficeRequest,
)