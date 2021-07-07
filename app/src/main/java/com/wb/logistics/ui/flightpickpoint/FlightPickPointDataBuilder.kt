package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightPickPointDataBuilder {
    fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: PickupPointBoxGroupByOfficeEntity, index: Int,
    ): BaseItem

    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}