package com.wb.logistics.di.module

import com.wb.logistics.ui.auth.*
import com.wb.logistics.ui.config.ConfigViewModel
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesDataBuilder
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesDataBuilderImpl
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesResourceProvider
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesViewModel
import com.wb.logistics.ui.flights.FlightsDataBuilder
import com.wb.logistics.ui.flights.FlightsDataBuilderImpl
import com.wb.logistics.ui.flights.FlightsResourceProvider
import com.wb.logistics.ui.flights.FlightsViewModel
import com.wb.logistics.ui.nav.NavigationViewModel
import com.wb.logistics.ui.reception.*
import com.wb.logistics.utils.time.TimeFormatter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // TODO: 25.03.2021 вынести в отдельный модуль
    fun provideFlightsDataBuilder(
        timeFormatter: TimeFormatter,
        resourceProvider: FlightsResourceProvider,
    ): FlightsDataBuilder {
        return FlightsDataBuilderImpl(timeFormatter, resourceProvider)
    }

    fun provideFlightDeliveriesDataBuilder(
        resourceProvider: FlightDeliveriesResourceProvider,
    ): FlightDeliveriesDataBuilder {
        return FlightDeliveriesDataBuilderImpl(resourceProvider)
    }

    single { provideFlightsDataBuilder(get(), get()) }
    single { provideFlightDeliveriesDataBuilder(get()) }

    viewModel { NumberPhoneViewModel(get(), get(), get()) }
    viewModel { ConfigViewModel(get(), get()) }
    viewModel { (parameters: TemporaryPasswordParameters) ->
        TemporaryPasswordViewModel(parameters, get(), get(), get())
    }
    viewModel { (parameters: InputPasswordParameters) ->
        InputPasswordViewModel(parameters, get(), get())
    }
    viewModel { (parameters: CreatePasswordParameters) ->
        CreatePasswordViewModel(parameters, get(), get())
    }
    viewModel { (parameters: ReceptionBoxNotBelongParameters) ->
        ReceptionBoxNotBelongModel(parameters)
    }
    viewModel { NavigationViewModel(get(), get(), get(), get(), get()) }
    viewModel { FlightsViewModel(get(), get(), get(), get(), get()) }
    viewModel { ReceptionScanViewModel(get(), get(), get(), get()) }
    viewModel { ReceptionHandleModel(get(), get()) }

    viewModel { ReceptionBoxesViewModel(get(), get()) }

    viewModel { FlightDeliveriesViewModel(get(), get(), get(), get(), get()) }
}