package com.wb.logistics.ui.flightdeliveriesdetails.domain

import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity

data class UnloadedAndReturnBoxesGroupByOffice(
    val errorBoxes: List<DeliveryErrorBoxEntity>,
    val unloadedBoxes: List<FlightBoxEntity>,
    val returnBoxes: List<FlightBoxEntity>,
)
