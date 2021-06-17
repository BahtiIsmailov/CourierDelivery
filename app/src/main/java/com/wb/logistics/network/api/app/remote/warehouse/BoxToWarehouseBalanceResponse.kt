package com.wb.logistics.network.api.app.remote.warehouse

data class BoxToWarehouseBalanceResponse(
    val srcOffice: BoxToWarehouseBalanceSrcOfficeResponse,
    val dstOffice: BoxToWarehouseBalanceDstOfficeResponse,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
)