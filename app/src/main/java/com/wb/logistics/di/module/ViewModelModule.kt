package com.wb.logistics.di.module

import com.wb.logistics.ui.auth.*
import com.wb.logistics.ui.config.ConfigViewModel
import com.wb.logistics.ui.flights.FlightResourceProvider
import com.wb.logistics.ui.flights.FlightsDataBuilder
import com.wb.logistics.ui.flights.FlightsDataBuilderImpl
import com.wb.logistics.ui.flights.FlightsViewModel
import com.wb.logistics.ui.nav.NavigationViewModel
import com.wb.logistics.ui.reception.ReceptionViewModel
import com.wb.logistics.utils.time.TimeFormatter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {


    // TODO: 25.03.2021 вынести в отдельный модуль
    fun provideFlightsDataBuilder(
        timeFormatter: TimeFormatter,
        resourceProvider: FlightResourceProvider
    ): FlightsDataBuilder {
        return FlightsDataBuilderImpl(timeFormatter, resourceProvider)
    }

    single { provideFlightsDataBuilder(get(), get()) }


    viewModel { NumberPhoneViewModel(get(), get(), get()) }
    viewModel { ConfigViewModel(get(), get()) }
    viewModel { (parameters: TemporaryPasswordParameters) ->
        TemporaryPasswordViewModel(
            parameters,
            get(),
            get(),
            get()
        )
    }
    viewModel { (parameters: InputPasswordParameters) ->
        InputPasswordViewModel(
            parameters,
            get(),
            get()
        )
    }
    viewModel { (parameters: CreatePasswordParameters) ->
        CreatePasswordViewModel(
            parameters,
            get(),
            get()
        )
    }
    viewModel { NavigationViewModel(get(), get(), get(), get()) }
    viewModel { FlightsViewModel(get(), get(), get(), get()) }
    viewModel { ReceptionViewModel(get()) }
}