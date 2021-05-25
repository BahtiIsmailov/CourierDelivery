package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadedBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity

data class DcBoxDefinitionResult(
    val flight: SuccessOrEmptyData<FlightEntity>,
    val findDcUnloadedBox: SuccessOrEmptyData<DcUnloadedBoxEntity>,
    val findReturnBox: SuccessOrEmptyData<ReturnBoxEntity>,
    val findAttachedBox: SuccessOrEmptyData<AttachedBoxEntity>,
    val barcodeScanned: String,
    val isManualInput: Boolean,
)
