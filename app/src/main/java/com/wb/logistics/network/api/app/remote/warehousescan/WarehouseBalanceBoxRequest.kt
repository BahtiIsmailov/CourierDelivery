package com.wb.logistics.network.api.app.remote.warehousescan

data class WarehouseBalanceBoxRequest(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: WarehouseBalanceBoxCurrentOfficeRequest,
)