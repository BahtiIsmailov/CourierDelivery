package com.wb.logistics.network.api.app.remote.warehouse

data class BoxFromWarehouseBalanceResponse(
    val srcOffice: BoxFromWarehouseBalanceSrcOfficeResponse,
    val dstOffice: BoxFromWarehouseBalanceDstOfficeResponse,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
)