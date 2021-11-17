package ru.wb.go.ui.flightpickpoint

import ru.wb.go.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.go.mvvm.model.base.BaseItem

interface FlightPickPointDataBuilder {
    fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: PickupPointBoxGroupByOfficeEntity, index: Int,
    ): BaseItem

    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}