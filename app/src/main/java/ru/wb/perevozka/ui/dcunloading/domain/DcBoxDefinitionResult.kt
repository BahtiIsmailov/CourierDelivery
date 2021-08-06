package ru.wb.perevozka.ui.dcunloading.domain

import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.flight.FlightEntity

data class DcBoxDefinitionResult(
    val flight: FlightEntity,
    val warehouseScanOptional: Optional<FlightBoxEntity>,
    val barcode: String,
    val isManual: Boolean,
    val updatedAt: String,
    val codeError: String
)
