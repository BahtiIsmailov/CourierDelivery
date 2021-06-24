package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightPickPointDataBuilder {
    fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: DeliveryBoxGroupByOfficeEntity, index: Int,
    ): BaseItem

    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}