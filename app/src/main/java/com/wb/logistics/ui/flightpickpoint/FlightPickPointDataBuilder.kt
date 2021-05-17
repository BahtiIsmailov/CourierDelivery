package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightPickPointDataBuilder {
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