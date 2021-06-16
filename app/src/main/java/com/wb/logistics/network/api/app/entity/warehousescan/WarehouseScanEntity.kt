package com.wb.logistics.network.api.app.entity.warehousescan

import com.wb.logistics.db.entity.flighboxes.BoxStatus

data class WarehouseScanEntity(
    val srcOffice: WarehouseScanSrcOfficeEntity,
    val dstOffice: WarehouseScanDstOfficeEntity,
    val barcode: String,
    val updatedAt: String,
    val status: BoxStatus,
)