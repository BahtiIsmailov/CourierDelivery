package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.*

class FlightDeliveriesDataBuilderImpl(
    private val resourceProvider: FlightDeliveriesResourceProvider,
) : FlightDeliveriesDataBuilder {

    override fun buildPvzSuccessItem(
        index: Int,
        scannedBoxGroupByAddressEntity: DeliveryBoxGroupByOfficeEntity,
    ): BaseItem {
        val unloadedCount = scannedBoxGroupByAddressEntity.unloadedCount
        val attachedCount = scannedBoxGroupByAddressEntity.attachedCount
        val returnCount = scannedBoxGroupByAddressEntity.returnCount
        if (returnCount > 0 || unloadedCount > 0) {
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