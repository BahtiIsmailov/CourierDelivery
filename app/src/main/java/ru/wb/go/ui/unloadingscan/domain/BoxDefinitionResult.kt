package ru.wb.go.ui.unloadingscan.domain

import ru.wb.go.db.Optional
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.db.entity.flight.FlightEntity
import ru.wb.go.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity

data class BoxDefinitionResult(
    val flight: FlightEntity,
    val findFlightBox: Optional<FlightBoxEntity>,
    val findPvzMatchingBox: Optional<PvzMatchingBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
