package ru.wb.perevozka.ui.flightpickpoint

import ru.wb.perevozka.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem
import ru.wb.perevozka.ui.flightpickpoint.delegates.items.FlightPickPointItem

class FlightPickPointDataBuilderImpl(
    private val resourceProvider: FlightPickPointResourceProvider,
) : FlightPickPointDataBuilder {

    override fun buildSuccessItem(
        flightPickupPointBoxGroupByOffice: PickupPointBoxGroupByOfficeEntity,
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