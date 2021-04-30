package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightDeliveriesDataBuilder {
    fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: AttachedBoxGroupByOfficeEntity,
        isEnabled: Boolean,
        index: Int
    ): BaseItem

    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}