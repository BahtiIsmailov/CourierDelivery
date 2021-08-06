package ru.wb.perevozka.ui.flightdeliveries

import ru.wb.perevozka.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.perevozka.mvvm.model.base.BaseItem

interface FlightDeliveriesDataBuilder {
    fun buildPvzSuccessItem(
        index: Int, scannedBoxGroupByAddressEntity: DeliveryBoxGroupByOfficeEntity,
    ): BaseItem

    fun buildEmptyItem(): BaseItem
    fun buildProgressItem(): BaseItem
    fun buildErrorItem(): BaseItem
    fun buildErrorMessageItem(message: String): BaseItem
}