package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity

data class BoxDefinitionResult(
    val flight: SuccessOrEmptyData<FlightEntity>,
    val findUnloadedBox: SuccessOrEmptyData<UnloadedBoxEntity>,
    val findReturnBox: SuccessOrEmptyData<ReturnBoxEntity>,
    val findAttachedBox: SuccessOrEmptyData<AttachedBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
