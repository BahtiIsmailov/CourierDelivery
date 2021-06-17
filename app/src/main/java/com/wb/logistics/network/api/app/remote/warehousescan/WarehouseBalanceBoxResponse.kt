package com.wb.logistics.network.api.app.remote.warehousescan

data class WarehouseBalanceBoxResponse(
    val srcOffice: WarehouseBalanceBoxSrcOfficeResponse,
    val dstOffice: WarehouseBalanceBoxDstOfficeResponse,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
)