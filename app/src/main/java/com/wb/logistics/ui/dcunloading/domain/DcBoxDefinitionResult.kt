package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity

data class DcBoxDefinitionResult(
    val flight: Optional<FlightEntity>,
    val findDcUnloadedBox: Optional<DcUnloadedBoxEntity>,
    val findReturnBox: Optional<ReturnBoxEntity>,
    val findAttachedBox: Optional<AttachedBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
