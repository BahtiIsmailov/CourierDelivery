package ru.wb.go.network.api.app.entity.warehousescan

import ru.wb.go.db.entity.flighboxes.BoxStatus

data class WarehouseScanEntity(
    val srcOffice: WarehouseScanSrcOfficeEntity,
    val dstOffice: WarehouseScanDstOfficeEntity,
    val barcode: String,
    val updatedAt: String,
    val status: BoxStatus,
)