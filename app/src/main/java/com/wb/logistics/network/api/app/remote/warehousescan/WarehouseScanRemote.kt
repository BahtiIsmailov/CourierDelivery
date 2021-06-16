package com.wb.logistics.network.api.app.remote.warehousescan

data class WarehouseScanRemote(
    val srcOffice: WarehouseScanSrcOfficeRemote,
    val dstOffice: WarehouseScanDstOfficeRemote,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
)