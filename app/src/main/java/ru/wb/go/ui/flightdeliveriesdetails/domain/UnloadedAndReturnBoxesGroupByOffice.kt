package ru.wb.go.ui.flightdeliveriesdetails.domain

import ru.wb.go.db.entity.deliveryerrorbox.DeliveryUnloadingErrorBoxEntity
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity

data class UnloadedAndReturnBoxesGroupByOffice(
    val unloadedBoxes: List<DeliveryUnloadingErrorBoxEntity>,
    val returnBoxes: List<FlightBoxEntity>,
)
