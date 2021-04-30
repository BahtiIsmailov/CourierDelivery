package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesProgressItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem

class FlightDeliveriesDataBuilderImpl(
    private val resourceProvider: FlightDeliveriesResourceProvider,
) : FlightDeliveriesDataBuilder {

    override fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: AttachedBoxGroupByOfficeEntity,
        isEnabled: Boolean,
        index: Int,
    ): BaseItem {
        val undoCount = scannedBoxGroupByAddressEntity.undoCount
        return FlightDeliveriesItem(
            address = scannedBoxGroupByAddressEntity.dstFullAddress,
            redoCount = resourceProvider.getRedoCount(scannedBoxGroupByAddressEntity.redoCount),
            undoCount =  if (undoCount == 0) resourceProvider.getEmptyCount() else resourceProvider.getUndoCount(undoCount),
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