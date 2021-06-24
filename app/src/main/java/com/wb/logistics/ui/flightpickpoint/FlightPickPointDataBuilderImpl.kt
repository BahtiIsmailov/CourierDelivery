package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem
import com.wb.logistics.ui.flightpickpoint.delegates.items.FlightPickPointItem

class FlightPickPointDataBuilderImpl(
    private val resourceProvider: FlightPickPointResourceProvider,
) : FlightPickPointDataBuilder {

    override fun buildSuccessItem(
        scannedBoxGroupByAddressEntity: DeliveryBoxGroupByOfficeEntity, index: Int,
    ): BaseItem {
        return FlightPickPointItem(
            address = scannedBoxGroupByAddressEntity.dstFullAddress,
            redoCount = resourceProvider.getRedoCount(scannedBoxGroupByAddressEntity.attachedCount),
            isShowBoxes = false,
            boxes = listOf(),
            idView = index
        )
    }

    override fun buildErrorItem() =
        FlightDeliveriesRefreshItem(resourceProvider.getFlightListError())

    override fun buildErrorMessageItem(message: String) = FlightDeliveriesRefreshItem(message)

}