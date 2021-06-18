package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity

data class BoxDefinitionResult(
    val flight: FlightEntity,
    val warehouseMatchingBoxOptional: Optional<WarehouseMatchingBoxEntity>,
    val attachedBoxOptional: Optional<AttachedBoxEntity>,
    val warehouseScanOptional: Optional<WarehouseScanEntity>,
    val barcode: String,
    val isManual: Boolean,
    val updatedAt: String,
    val codeError: String
) {
    constructor(
        flight: FlightEntity,
        warehouseMatchingBoxOptional: Optional<WarehouseMatchingBoxEntity>,
        attachedBoxOptional: Optional<AttachedBoxEntity>,
        barcode: String,
        isManual: Boolean,
        updatedAt: String,
    ) : this(flight,
        warehouseMatchingBoxOptional,
        attachedBoxOptional,
        Optional.Empty(),
        barcode,
        isManual,
        updatedAt,
    "")
}
