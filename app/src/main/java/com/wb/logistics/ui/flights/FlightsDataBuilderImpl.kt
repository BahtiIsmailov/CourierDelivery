package com.wb.logistics.ui.flights

import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flights.delegates.items.FlightItem
import com.wb.logistics.ui.flights.delegates.items.FlightProgressItem
import com.wb.logistics.ui.flights.delegates.items.FlightRefreshItem
import com.wb.logistics.ui.flights.domain.FlightEntity
import com.wb.logistics.ui.flights.domain.FlightsData
import com.wb.logistics.utils.time.TimeFormatType.ONLY_DATE
import com.wb.logistics.utils.time.TimeFormatType.ONLY_TIME
import com.wb.logistics.utils.time.TimeFormatter

class FlightsDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: FlightResourceProvider,
) : FlightsDataBuilder {

    override fun buildSuccessItem(flightEntity: FlightEntity.Success<FlightsData>): BaseItem {
        return with(flightEntity.data) {
            val flightOffices =
                if (offices.isEmpty()) listOf(resourceProvider.getRoutesEmpty())
                else offices
            FlightItem(
                resourceProvider.getFlightNumber(flight),
                resourceProvider.getParkingNumber(parkingNumber),
                timeFormatter.format(date, ONLY_DATE),
                timeFormatter.format(date, ONLY_TIME),
                resourceProvider.getRoutesTitle(routesTitle),
                flightOffices
            )
        }
    }

    override fun buildEmptyItem(): BaseItem = FlightRefreshItem(resourceProvider.getEmptyFlight())

    override fun buildProgressItem(): BaseItem = FlightProgressItem()

    override fun buildErrorItem(): BaseItem = FlightRefreshItem(resourceProvider.getErrorFlight())

    override fun buildErrorMessageItem(message: String): BaseItem = FlightRefreshItem(message)

}