package ru.wb.go.ui.flightdeliveries

import ru.wb.go.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.go.mvvm.model.base.BaseItem

interface FlightDeliveriesDataBuilder {
    fun buildPvzSuccessItem(
        index: Int, scannedBoxGroupByAddressEntity: DeliveryBoxGroupByOfficeEntity,
    ): BaseItem

    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}