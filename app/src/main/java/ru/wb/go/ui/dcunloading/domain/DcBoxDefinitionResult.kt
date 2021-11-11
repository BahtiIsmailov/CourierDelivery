package ru.wb.go.ui.dcunloading.domain

import ru.wb.go.db.Optional
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.db.entity.flight.FlightEntity

data class DcBoxDefinitionResult(
    val flight: FlightEntity,
    val warehouseScanOptional: Optional<FlightBoxEntity>,
    val barcode: String,
    val isManual: Boolean,
    val updatedAt: String,
    val codeError: String
)
