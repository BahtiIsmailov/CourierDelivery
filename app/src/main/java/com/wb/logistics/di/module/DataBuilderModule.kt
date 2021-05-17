package com.wb.logistics.di.module

import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesDataBuilder
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesDataBuilderImpl
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesResourceProvider
import com.wb.logistics.ui.flightpickpoint.FlightPickPointDataBuilder
import com.wb.logistics.ui.flightpickpoint.FlightPickPointDataBuilderImpl
import com.wb.logistics.ui.flightpickpoint.FlightPickPointResourceProvider
import com.wb.logistics.ui.flights.FlightsDataBuilder
import com.wb.logistics.ui.flights.FlightsDataBuilderImpl
import com.wb.logistics.ui.flights.FlightsResourceProvider
import com.wb.logistics.ui.forcedtermination.ForcedTerminationDataBuilder
import com.wb.logistics.ui.forcedtermination.ForcedTerminationDataBuilderImpl
import com.wb.logistics.ui.forcedtermination.ForcedTerminationResourceProvider
import com.wb.logistics.utils.time.TimeFormatter
import org.koin.dsl.module

val dataBuilderModule = module {

    fun provideFlightsDataBuilder(
        timeFormatter: TimeFormatter,
        resourceProvider: FlightsResourceProvider,
    ): FlightsDataBuilder {
        return FlightsDataBuilderImpl(timeFormatter, resourceProvider)
    }

    fun provideFlightPickPointDataBuilder(
        resourceProvider: FlightPickPointResourceProvider,
    ): FlightPickPointDataBuilder {
        return FlightPickPointDataBuilderImpl(resourceProvider)
    }

    fun provideFlightDeliveriesDataBuilder(
        resourceProvider: FlightDeliveriesResourceProvider,
    ): FlightDeliveriesDataBuilder {
        return FlightDeliveriesDataBuilderImpl(resourceProvider)
    }

    fun provideForcedTerminationDataBuilder(
        timeFormatter: TimeFormatter,
        resourceProvider: ForcedTerminationResourceProvider,
    ): ForcedTerminationDataBuilder {
        return ForcedTerminationDataBuilderImpl(timeFormatter, resourceProvider)
    }

    single { provideFlightsDataBuilder(get(), get()) }
    single { provideFlightPickPointDataBuilder(get()) }
    single { provideFlightDeliveriesDataBuilder(get()) }
    single { provideForcedTerminationDataBuilder(get(), get()) }

}