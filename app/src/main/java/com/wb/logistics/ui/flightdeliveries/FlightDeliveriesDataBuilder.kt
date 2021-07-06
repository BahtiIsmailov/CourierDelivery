package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem

interface FlightDeliveriesDataBuilder {
    fun buildPvzSuccessItem(
        index: Int, scannedBoxGroupByAddressEntity: DeliveryBoxGroupByOfficeEntity,
    ): BaseItem

    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}