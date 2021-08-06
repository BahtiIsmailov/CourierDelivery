package ru.wb.perevozka.ui.flightpickpoint

import ru.wb.perevozka.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.perevozka.mvvm.model.base.BaseItem

interface FlightPickPointDataBuilder {
    fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: PickupPointBoxGroupByOfficeEntity, index: Int,
    ): BaseItem

    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}