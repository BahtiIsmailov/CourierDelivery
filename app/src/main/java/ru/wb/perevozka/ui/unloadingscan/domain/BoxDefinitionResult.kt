package ru.wb.perevozka.ui.unloadingscan.domain

import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.flight.FlightEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity

data class BoxDefinitionResult(
    val flight: FlightEntity,
    val findFlightBox: Optional<FlightBoxEntity>,
    val findPvzMatchingBox: Optional<PvzMatchingBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
