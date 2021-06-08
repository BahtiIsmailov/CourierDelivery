package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity

data class BoxDefinitionResult(
    val flight: SuccessOrEmptyData<FlightEntity>,
    val flightBoxHasBeenScanned: SuccessOrEmptyData<AttachedBoxEntity>,
    val matchingBox: SuccessOrEmptyData<MatchingBoxEntity>,
    val barcode: String,
    val isManual: Boolean,
)
