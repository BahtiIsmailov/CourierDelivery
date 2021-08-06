package ru.wb.perevozka.ui.flights

import ru.wb.perevozka.db.FlightData
import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.flights.delegates.items.FlightItem
import ru.wb.perevozka.ui.flights.delegates.items.FlightProgressItem
import ru.wb.perevozka.ui.flights.delegates.items.FlightRefreshItem
import ru.wb.perevozka.utils.time.TimeFormatType.ONLY_DATE
import ru.wb.perevozka.utils.time.TimeFormatType.ONLY_TIME
import ru.wb.perevozka.utils.time.TimeFormatter

class FlightsDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: FlightsResourceProvider,
) : FlightsDataBuilder {

    override fun buildSuccessItem(flightEntity: Optional.Success<FlightData>): BaseItem {
        return with(flightEntity.data) {
            val flightOffices =
                if (offices.isEmpty()) listOf(resourceProvider.getRoutesEmpty())
                else offices
            val date = timeFormatter.dateTimeWithoutTimezoneFromString(date)
            FlightItem(
                resourceProvider.getFlightNumber(flightId),
                resourceProvider.getParkingNumber(gate),
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