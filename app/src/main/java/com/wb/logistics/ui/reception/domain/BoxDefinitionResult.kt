package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity

data class BoxDefinitionResult(
    val flight: SuccessOrEmptyData<FlightEntity>,
    val flightBoxHasBeenScanned: SuccessOrEmptyData<AttachedBoxEntity>,
    val matchingBox: SuccessOrEmptyData<MatchingBoxEntity>,
    val barcodeScanned: String,
    val isManual: Boolean,
)