package com.wb.logistics.di.module

import com.wb.logistics.ui.auth.*
import com.wb.logistics.ui.config.ConfigViewModel
import com.wb.logistics.ui.dcforcedtermination.DcForcedTerminationDetailsViewModel
import com.wb.logistics.ui.dcforcedtermination.DcForcedTerminationViewModel
import com.wb.logistics.ui.dcloading.*
import com.wb.logistics.ui.dcunloading.*
import com.wb.logistics.ui.dcunloadingcongratulation.DcUnloadingCongratulationViewModel
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesViewModel
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsViewModel
import com.wb.logistics.ui.flightloader.FlightLoaderViewModel
import com.wb.logistics.ui.flightpickpoint.FlightPickPointViewModel
import com.wb.logistics.ui.flights.FlightsViewModel
import com.wb.logistics.ui.flightsempty.FlightsEmptyViewModel
import com.wb.logistics.ui.forcedtermination.ForcedTerminationParameters
import com.wb.logistics.ui.forcedtermination.ForcedTerminationViewModel
import com.wb.logistics.ui.scanner.ScannerViewModel
import com.wb.logistics.ui.splash.AppViewModel
import com.wb.logistics.ui.splash.LoaderViewModel
import com.wb.logistics.ui.unloading.*
import com.wb.logistics.ui.unloadingcongratulation.CongratulationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { AppViewModel(get(), get(), get(), get()) }

    viewModel { LoaderViewModel(get(), get(), get(), get(), get()) }

    viewModel { NumberPhoneViewModel(get(), get(), get(), get()) }
    viewModel { ConfigViewModel(get(), get()) }
    viewModel { (parameters: TemporaryPasswordParameters) ->
        TemporaryPasswordViewModel(parameters, get(), get(), get())
    }
    viewModel { (parameters: InputPasswordParameters) ->
        InputPasswordViewModel(parameters, get(), get(), get())
    }
    viewModel { (parameters: CreatePasswordParameters) ->
        CreatePasswordViewModel(parameters, get(), get(), get())
    }
    viewModel { (parameters: DcLoadingBoxNotBelongParameters) ->
        DcLoadingBoxNotBelongModel(parameters)
    }

    viewModel { FlightLoaderViewModel(get(), get(), get()) }
    viewModel { FlightsViewModel(get(), get(), get()) }
    viewModel { FlightsEmptyViewModel(get()) }

    viewModel { ScannerViewModel(get(), get(), get()) }
    viewModel { DcLoadingScanViewModel(get(), get(), get()) }
    viewModel { DcLoadingHandleViewModel(get(), get()) }

    viewModel { DcLoadingBoxesViewModel(get(), get()) }

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

    viewModel {
        CongratulationViewModel(
            get(),
            get(),
            get(),
            get())
    }

    viewModel {
        DcUnloadingScanViewModel(
            get(),
            get(),
            get(),
            get())
    }

    viewModel {
        DcUnloadingHandleViewModel(
            get(),
            get(),
            get())
    }

    viewModel { (parameters: DcUnloadingBoxNotBelongParameters) ->
        DcUnloadingBoxNotBelongModel(parameters)
    }

    viewModel { DcUnloadingBoxesViewModel(get(), get()) }

    viewModel { DcForcedTerminationViewModel(get(), get(), get()) }

    viewModel { DcForcedTerminationDetailsViewModel(get(), get(), get()) }

    viewModel { DcUnloadingCongratulationViewModel(get(), get(), get(), get()) }

}