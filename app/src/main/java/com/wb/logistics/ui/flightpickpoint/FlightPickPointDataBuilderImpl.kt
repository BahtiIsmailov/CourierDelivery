package com.wb.logistics.ui.flightpickpoint

import com.wb.logistics.db.entity.deliveryboxes.FlightPickupPointBoxGroupByOfficeEntity
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem
import com.wb.logistics.ui.flightpickpoint.delegates.items.FlightPickPointItem

class FlightPickPointDataBuilderImpl(
    private val resourceProvider: FlightPickPointResourceProvider,
) : FlightPickPointDataBuilder {

    override fun buildSuccessItem(
        flightPickupPointBoxGroupByOffice: FlightPickupPointBoxGroupByOfficeEntity,
        index: Int,
    ): BaseItem {
        with(flightPickupPointBoxGroupByOffice) {
            return FlightPickPointItem(
                address = dstFullAddress,
                deliverCount = resourceProvider.getDeliverCount(deliverCount),
                pickupCount = resourceProvider.getPickupCount(pickUpCount),
                isPickupPoint = pickUpCount > 0,
                idView = index
            )
        }
    }

    override fun buildErrorItem() =
        FlightDeliveriesRefreshItem(resourceProvider.getFlightListError())

    override fun buildErrorMessageItem(message: String) = FlightDeliveriesRefreshItem(message)

}