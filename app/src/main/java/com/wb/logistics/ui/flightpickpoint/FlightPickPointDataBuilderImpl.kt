package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesProgressItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem
import com.wb.logistics.ui.flightpickpoint.delegates.items.FlightPickPointItem

class FlightPickPointDataBuilderImpl(
    private val resourceProvider: FlightPickPointResourceProvider,
) : FlightPickPointDataBuilder {

    override fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: AttachedBoxGroupByOfficeEntity,
        isEnabled: Boolean,
        index: Int,
    ): BaseItem {
        return FlightPickPointItem(
            address = scannedBoxGroupByAddressEntity.dstFullAddress,
            redoCount = resourceProvider.getRedoCount(scannedBoxGroupByAddressEntity.attachedCount),
            isShowBoxes = false,
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