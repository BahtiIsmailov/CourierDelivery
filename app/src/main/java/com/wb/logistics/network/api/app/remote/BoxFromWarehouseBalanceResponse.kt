package com.wb.logistics.network.api.app.remote

data class BoxFromWarehouseBalanceResponse(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: BoxFromWarehouseBalanceCurrentOfficeRemote,
)