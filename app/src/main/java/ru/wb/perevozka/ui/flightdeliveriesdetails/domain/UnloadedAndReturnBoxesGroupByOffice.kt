package ru.wb.perevozka.ui.flightdeliveriesdetails.domain

import ru.wb.perevozka.db.entity.deliveryerrorbox.DeliveryUnloadingErrorBoxEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity

data class UnloadedAndReturnBoxesGroupByOffice(
    val unloadedBoxes: List<DeliveryUnloadingErrorBoxEntity>,
    val returnBoxes: List<FlightBoxEntity>,
)
