package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity

data class DcBoxDefinitionResult(
    val flight: FlightEntity,
    val warehouseScanOptional: Optional<FlightBoxEntity>,
    val barcode: String,
    val isManual: Boolean,
    val updatedAt: String,
    val codeError: String
)
