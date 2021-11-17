package ru.wb.go.di.module

import org.koin.dsl.module
import ru.wb.go.ui.courierbilling.CourierBillingDataBuilder
import ru.wb.go.ui.courierbilling.CourierBillingDataBuilderImpl
import ru.wb.go.ui.courierbilling.CourierBillingResourceProvider
import ru.wb.go.ui.courierorders.CourierOrdersDataBuilder
import ru.wb.go.ui.courierorders.CourierOrdersDataBuilderImpl
import ru.wb.go.ui.courierorders.CourierOrdersResourceProvider
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsDataBuilder
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsDataBuilderImpl
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsResourceProvider
import ru.wb.go.ui.flightdeliveries.FlightDeliveriesDataBuilder
import ru.wb.go.ui.flightdeliveries.FlightDeliveriesDataBuilderImpl
import ru.wb.go.ui.flightdeliveries.FlightDeliveriesResourceProvider
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsDataBuilder
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsDataBuilderImpl
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsResourceProvider
import ru.wb.go.ui.flightpickpoint.FlightPickPointDataBuilder
import ru.wb.go.ui.flightpickpoint.FlightPickPointDataBuilderImpl
import ru.wb.go.ui.flightpickpoint.FlightPickPointResourceProvider
import ru.wb.go.ui.flights.FlightsDataBuilder
import ru.wb.go.ui.flights.FlightsDataBuilderImpl
import ru.wb.go.ui.flights.FlightsResourceProvider
import ru.wb.go.ui.unloadingforcedtermination.ForcedTerminationDataBuilder
import ru.wb.go.ui.unloadingforcedtermination.ForcedTerminationDataBuilderImpl
import ru.wb.go.ui.unloadingforcedtermination.ForcedTerminationResourceProvider
import ru.wb.go.utils.time.TimeFormatter

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
        resourceProvider: CourierOrdersResourceProvider,
    ): CourierOrdersDataBuilder {
        return CourierOrdersDataBuilderImpl(resourceProvider)
    }

    fun provideCourierBillingDataBuilder(
        resourceProvider: CourierBillingResourceProvider,
        timeFormatter: TimeFormatter,
    ): CourierBillingDataBuilder {
        return CourierBillingDataBuilderImpl(resourceProvider, timeFormatter)
    }

    single { provideFlightsDataBuilder(get(), get()) }
    single { provideFlightPickPointDataBuilder(get()) }
    single { provideFlightDeliveriesDataBuilder(get()) }
    single { provideForcedTerminationDataBuilder(get(), get()) }
    single { provideFlightDeliveriesDetailsDataBuilder(get(), get()) }
    single { provideDcForcedTerminationDetailsDataBuilder(get(), get()) }
    single { provideCourierOrderDataBuilder(get()) }
    single { provideCourierBillingDataBuilder(get(), get()) }

}