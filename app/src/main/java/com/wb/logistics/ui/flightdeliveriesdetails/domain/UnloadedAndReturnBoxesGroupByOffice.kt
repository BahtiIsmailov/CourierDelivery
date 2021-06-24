package com.wb.logistics.ui.flightdeliveriesdetails.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity

data class UnloadedAndReturnBoxesGroupByOffice(
    val unloadedBoxes: List<FlightBoxEntity>,
    val returnBoxes: List<FlightBoxEntity>,
)
