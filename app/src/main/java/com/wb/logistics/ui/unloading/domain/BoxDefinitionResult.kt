package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity

data class BoxDefinitionResult(
    val flight: FlightEntity,
    val findFlightBox: Optional<FlightBoxEntity>,
    val findPvzMatchingBox: Optional<PvzMatchingBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
