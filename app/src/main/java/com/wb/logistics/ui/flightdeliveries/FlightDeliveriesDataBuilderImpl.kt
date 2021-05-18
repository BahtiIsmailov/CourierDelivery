package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.*

class FlightDeliveriesDataBuilderImpl(
    private val resourceProvider: FlightDeliveriesResourceProvider,
) : FlightDeliveriesDataBuilder {

    override fun buildSuccessItem(
        index: Int,
        scannedBoxGroupByAddressEntity: AttachedBoxGroupByOfficeEntity,
    ): BaseItem {
        val attachedCount = scannedBoxGroupByAddressEntity.attachedCount
        val returnCount = scannedBoxGroupByAddressEntity.returnCount
        val unloadedCount = scannedBoxGroupByAddressEntity.unloadedCount
        if (scannedBoxGroupByAddressEntity.isUnloading) {
            val returnCountText =
                if (returnCount > 0) resourceProvider.getReturnCount(returnCount)
                else resourceProvider.getEmptyCount()
            return if (attachedCount > 0) {
                FlightDeliveriesNotUnloadItem(
                    address = scannedBoxGroupByAddressEntity.dstFullAddress,
                    unloadedCount = resourceProvider.getNotDelivery(unloadedCount, unloadedCount + attachedCount),
                    returnCount = returnCountText,
                    idView = index)

            } else {
                FlightDeliveriesUnloadItem(
                    address = scannedBoxGroupByAddressEntity.dstFullAddress,
                    unloadedCount = resourceProvider.getDelivery(unloadedCount),
                    returnCount = returnCountText,
                    idView = index)
            }

        } else {
            return FlightDeliveriesItem(
                address = scannedBoxGroupByAddressEntity.dstFullAddress,
                redoCount = resourceProvider.getRedoCount(attachedCount),
                undoCount = if (returnCount == 0) resourceProvider.getEmptyCount()
                else resourceProvider.getUndoCount(returnCount),
                isShowBoxes = false,
                isEnabled = true,
                boxes = listOf(),
                idView = index
            )
        }
    }

    override fun buildEmptyItem(): BaseItem =
        FlightDeliveriesRefreshItem("Список пуст")

    override fun buildProgressItem(): BaseItem = FlightDeliveriesProgressItem()

    override fun buildErrorItem(): BaseItem =
        FlightDeliveriesRefreshItem("Ошибка получения данных")

    override fun buildErrorMessageItem(message: String): BaseItem =
        FlightDeliveriesRefreshItem(message)

}