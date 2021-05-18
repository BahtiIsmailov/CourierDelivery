package com.wb.logistics.di.module

import com.wb.logistics.ui.auth.*
import com.wb.logistics.ui.config.ConfigViewModel
import com.wb.logistics.ui.congratulation.CongratulationParameters
import com.wb.logistics.ui.congratulation.CongratulationViewModel
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesViewModel
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsViewModel
import com.wb.logistics.ui.flightpickpoint.FlightPickPointViewModel
import com.wb.logistics.ui.flights.FlightLoaderViewModel
import com.wb.logistics.ui.flights.FlightsViewModel
import com.wb.logistics.ui.forcedtermination.ForcedTerminationParameters
import com.wb.logistics.ui.forcedtermination.ForcedTerminationViewModel
import com.wb.logistics.ui.reception.*
import com.wb.logistics.ui.scanner.ScannerViewModel
import com.wb.logistics.ui.splash.AppViewModel
import com.wb.logistics.ui.splash.LoaderViewModel
import com.wb.logistics.ui.unloading.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { AppViewModel(get(), get(), get(), get()) }

    viewModel { LoaderViewModel(get(), get(), get(), get(), get()) }

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

    viewModel { FlightLoaderViewModel(get(), get()) }
    viewModel {
        FlightsViewModel(get(),
            get(),
            get(),
            get(),
            get())
    }
    viewModel { ScannerViewModel(get(), get(), get()) }
    viewModel { ReceptionScanViewModel(get(), get(), get(), get()) }
    viewModel { ReceptionHandleViewModel(get(), get()) }

    viewModel { ReceptionBoxesViewModel(get(), get()) }

    viewModel { FlightPickPointViewModel(get(), get(), get(), get(), get()) }
    viewModel { FlightDeliveriesViewModel(get(), get(), get(), get(), get()) }
    viewModel { (parameters: FlightDeliveriesDetailsParameters) ->
        FlightDeliveriesDetailsViewModel(parameters,
            get(),
            get(),
            get(),
            get())
    }

    viewModel { (parameters: UnloadingScanParameters) ->
        UnloadingScanViewModel(parameters,
            get(),
            get(),
            get(),
            get())
    }

    viewModel { (parameters: UnloadingBoxNotBelongParameters) ->
        UnloadingBoxNotBelongModel(parameters)
    }

    viewModel { (parameters: UnloadingBoxesParameters) ->
        UnloadingBoxesViewModel(parameters,
            get(),
            get(),
            get())
    }

    viewModel { (parameters: UnloadingReturnParameters) ->
        UnloadingReturnBoxesViewModel(parameters,
            get(),
            get(),
            get())
    }
    viewModel { (parameters: UnloadingHandleParameters) ->
        UnloadingHandleViewModel(parameters,
            get(),
            get(),
            get())
    }

    viewModel { (parameters: ForcedTerminationParameters) ->
        ForcedTerminationViewModel(parameters,
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { (parameters: CongratulationParameters) ->
        CongratulationViewModel(parameters,
            get(),
            get(),
            get())
    }

}