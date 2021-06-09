package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity

data class BoxDefinitionResult(
    val flight: Optional<FlightEntity>,
    val flightBoxHasBeenScanned: Optional<AttachedBoxEntity>,
    val matchingBox: Optional<MatchingBoxEntity>,
    val barcode: String,
    val isManual: Boolean,
)
