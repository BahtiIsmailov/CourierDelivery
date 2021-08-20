package ru.wb.perevozka.di.module

import org.koin.dsl.module
import ru.wb.perevozka.ui.courierorders.CourierOrderDataBuilder
import ru.wb.perevozka.ui.courierorders.CourierOrderDataBuilderImpl
import ru.wb.perevozka.ui.courierorders.CourierOrderResourceProvider
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsDataBuilder
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsDataBuilderImpl
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsResourceProvider
import ru.wb.perevozka.ui.flightdeliveries.FlightDeliveriesDataBuilder
import ru.wb.perevozka.ui.flightdeliveries.FlightDeliveriesDataBuilderImpl
import ru.wb.perevozka.ui.flightdeliveries.FlightDeliveriesResourceProvider
import ru.wb.perevozka.ui.flightdeliveriesdetails.FlightDeliveriesDetailsDataBuilder
import ru.wb.perevozka.ui.flightdeliveriesdetails.FlightDeliveriesDetailsDataBuilderImpl
import ru.wb.perevozka.ui.flightdeliveriesdetails.FlightDeliveriesDetailsResourceProvider
import ru.wb.perevozka.ui.flightpickpoint.FlightPickPointDataBuilder
import ru.wb.perevozka.ui.flightpickpoint.FlightPickPointDataBuilderImpl
import ru.wb.perevozka.ui.flightpickpoint.FlightPickPointResourceProvider
import ru.wb.perevozka.ui.flights.FlightsDataBuilder
import ru.wb.perevozka.ui.flights.FlightsDataBuilderImpl
import ru.wb.perevozka.ui.flights.FlightsResourceProvider
import ru.wb.perevozka.ui.unloadingforcedtermination.ForcedTerminationDataBuilder
import ru.wb.perevozka.ui.unloadingforcedtermination.ForcedTerminationDataBuilderImpl
import ru.wb.perevozka.ui.unloadingforcedtermination.ForcedTerminationResourceProvider
import ru.wb.perevozka.utils.time.TimeFormatter

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

    fun provideFlightDeliveriesDetailsDataBuilder(
        timeFormatter: TimeFormatter,
        resourceProvider: FlightDeliveriesDetailsResourceProvider,
    ): FlightDeliveriesDetailsDataBuilder {
        return FlightDeliveriesDetailsDataBuilderImpl(timeFormatter, resourceProvider)
    }

    fun provideDcForcedTerminationDetailsDataBuilder(
        timeFormatter: TimeFormatter,
        resourceProvider: DcForcedTerminationDetailsResourceProvider,
    ): DcForcedTerminationDetailsDataBuilder {
        return DcForcedTerminationDetailsDataBuilderImpl(timeFormatter, resourceProvider)
    }


    fun provideCourierOrderDataBuilder(
        resourceProvider: CourierOrderResourceProvider,
    ): CourierOrderDataBuilder {
        return CourierOrderDataBuilderImpl(resourceProvider)
    }

    single { provideFlightsDataBuilder(get(), get()) }
    single { provideFlightPickPointDataBuilder(get()) }
    single { provideFlightDeliveriesDataBuilder(get()) }
    single { provideForcedTerminationDataBuilder(get(), get()) }
    single { provideFlightDeliveriesDetailsDataBuilder(get(), get()) }
    single { provideDcForcedTerminationDetailsDataBuilder(get(), get()) }
    single { provideCourierOrderDataBuilder(get()) }

}