package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity

data class BoxDefinitionResult(
    val flight: FlightEntity,
    val flightBoxOptional: Optional<FlightBoxEntity>,
    val warehouseScanOptional: Optional<WarehouseScanEntity>,
    val barcode: String,
    val isManual: Boolean,
    val updatedAt: String,
    val codeError: String
) {
    constructor(
        flight: FlightEntity,
        flightBoxOptional: Optional<FlightBoxEntity>,
        barcode: String,
        isManual: Boolean,
        updatedAt: String,
    ) : this(flight,
        flightBoxOptional,
        Optional.Empty(),
        barcode,
        isManual,
        updatedAt,
    "")
}
