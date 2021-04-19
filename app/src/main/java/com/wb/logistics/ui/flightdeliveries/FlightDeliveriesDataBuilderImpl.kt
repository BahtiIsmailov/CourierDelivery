package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesProgressItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem

class FlightDeliveriesDataBuilderImpl(
    private val resourceProvider: FlightDeliveriesResourceProvider,
) : FlightDeliveriesDataBuilder {

    override fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: ScannedBoxGroupByAddressEntity,
        isEnabled: Boolean,
        index: Int,
    ): BaseItem {

        return FlightDeliveriesItem(
            address = scannedBoxGroupByAddressEntity.dstFullAddress,
            redoCount = resourceProvider.getRedoCount(scannedBoxGroupByAddressEntity.count),
            isShowBoxes = false,
            isEnabled = isEnabled,
            boxes = listOf(),
            idView = index
        )
    }

    override fun buildEmptyItem(): BaseItem =
        FlightDeliveriesRefreshItem("Список пуст")

    override fun buildProgressItem(): BaseItem = FlightDeliveriesProgressItem()

    override fun buildErrorItem(): BaseItem =
        FlightDeliveriesRefreshItem("Ошибка получения данных")

    override fun buildErrorMessageItem(message: String): BaseItem =
        FlightDeliveriesRefreshItem(message)

}