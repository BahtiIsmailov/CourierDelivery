package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity

data class BoxDefinitionResult(
    val flight: Optional<FlightEntity>,
    val findUnloadedBox: Optional<UnloadedBoxEntity>,
    val findReturnBox: Optional<ReturnBoxEntity>,
    val findAttachedBox: Optional<AttachedBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
