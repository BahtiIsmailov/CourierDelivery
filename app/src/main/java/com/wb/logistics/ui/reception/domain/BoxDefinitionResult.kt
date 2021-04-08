package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity

data class BoxDefinitionResult(
    val flight: SuccessOrEmptyData<FlightEntity>,
    val flightBoxHasBeenScanned: SuccessOrEmptyData<FlightBoxScannedEntity>,
    val flightBox: SuccessOrEmptyData<FlightBoxEntity>,
    val barcodeScanned: String,
    val isManual: Boolean,
)
