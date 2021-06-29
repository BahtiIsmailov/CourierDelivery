package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity

data class DcBoxDefinitionResult(
    val flight: FlightEntity,
    val findDcUnloadedBox: Optional<FlightBoxEntity>,
    val findDcReturnBox: Optional<FlightBoxEntity>,
    val findAttachedBox: Optional<AttachedBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
