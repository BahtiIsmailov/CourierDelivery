package com.wb.logistics.ui.flightdeliveriesdetails.domain

import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryUnloadingErrorBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity

data class UnloadedAndReturnBoxesGroupByOffice(
    val unloadedBoxes: List<DeliveryUnloadingErrorBoxEntity>,
    val returnBoxes: List<FlightBoxEntity>,
)
