package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.deliveryboxes.FlightPickupPointBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightPickPointDataBuilder {
    fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: FlightPickupPointBoxGroupByOfficeEntity, index: Int,
    ): BaseItem

    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}