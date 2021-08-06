package ru.wb.perevozka.network.api.app.entity.warehousescan

import ru.wb.perevozka.db.entity.flighboxes.BoxStatus

data class WarehouseScanEntity(
    val srcOffice: WarehouseScanSrcOfficeEntity,
    val dstOffice: WarehouseScanDstOfficeEntity,
    val barcode: String,
    val updatedAt: String,
    val status: BoxStatus,
)