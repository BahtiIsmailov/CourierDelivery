package ru.wb.go.ui.dcloading.domain

import ru.wb.go.db.Optional
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.db.entity.flight.FlightEntity
import ru.wb.go.network.api.app.entity.warehousescan.WarehouseScanEntity

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
