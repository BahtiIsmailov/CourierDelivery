package ru.wb.perevozka.di.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.wb.perevozka.ui.auth.CheckSmsParameters
import ru.wb.perevozka.ui.auth.CheckSmsViewModel
import ru.wb.perevozka.ui.auth.NumberPhoneViewModel
import ru.wb.perevozka.ui.config.ConfigViewModel
import ru.wb.perevozka.ui.dcloading.*
import ru.wb.perevozka.ui.dcunloading.*
import ru.wb.perevozka.ui.dcunloadingcongratulation.DcUnloadingCongratulationViewModel
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsViewModel
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationViewModel
import ru.wb.perevozka.ui.flightdeliveries.FlightDeliveriesViewModel
import ru.wb.perevozka.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import ru.wb.perevozka.ui.flightdeliveriesdetails.FlightDeliveriesDetailsViewModel
import ru.wb.perevozka.ui.flightpickpoint.FlightPickPointViewModel
import ru.wb.perevozka.ui.flights.FlightsViewModel
import ru.wb.perevozka.ui.flightsloader.FlightLoaderViewModel
import ru.wb.perevozka.ui.scanner.ScannerViewModel
import ru.wb.perevozka.ui.splash.AppViewModel
import ru.wb.perevozka.ui.splash.LoaderViewModel
import ru.wb.perevozka.ui.unloadingboxes.UnloadingBoxesParameters
import ru.wb.perevozka.ui.unloadingboxes.UnloadingBoxesViewModel
import ru.wb.perevozka.ui.unloadingcongratulation.CongratulationViewModel
import ru.wb.perevozka.ui.unloadingforcedtermination.ForcedTerminationParameters
import ru.wb.perevozka.ui.unloadingforcedtermination.ForcedTerminationViewModel
import ru.wb.perevozka.ui.unloadinghandle.UnloadingHandleParameters
import ru.wb.perevozka.ui.unloadinghandle.UnloadingHandleViewModel
import ru.wb.perevozka.ui.unloadingreturnboxes.UnloadingReturnBoxesViewModel
import ru.wb.perevozka.ui.unloadingreturnboxes.UnloadingReturnParameters
import ru.wb.perevozka.ui.unloadingscan.UnloadingBoxNotBelongModel
import ru.wb.perevozka.ui.unloadingscan.UnloadingBoxNotBelongParameters
import ru.wb.perevozka.ui.unloadingscan.UnloadingScanParameters
import ru.wb.perevozka.ui.unloadingscan.UnloadingScanViewModel
import ru.wb.perevozka.ui.userdata.couriers.CouriersCompleteRegistrationParameters
import ru.wb.perevozka.ui.userdata.couriers.CouriersCompleteRegistrationViewModel
import ru.wb.perevozka.ui.userdata.userform.UserFormParameters
import ru.wb.perevozka.ui.userdata.userform.UserFormViewModel

val viewModelModule = module {

    viewModel { AppViewModel(get(), get(), get(), get()) }

    viewModel { LoaderViewModel(get(), get(), get(), get(), get()) }

    viewModel { NumberPhoneViewModel(get(), get(), get(), get()) }
    viewModel { ConfigViewModel(get(), get()) }

    viewModel { (parameters: CheckSmsParameters) ->
        CheckSmsViewModel(parameters, get(), get(), get())
    }

    viewModel { (parameters: UserFormParameters) ->
        UserFormViewModel(parameters, get(), get(), get())
    }

    viewModel { (parameters: CouriersCompleteRegistrationParameters) ->
        CouriersCompleteRegistrationViewModel(
            parameters,
            get(),
            get(),
            get()
        )
    }

    viewModel { ScannerViewModel(get(), get()) }

    viewModel { DcLoadingScanViewModel(get(), get(), get()) }
    viewModel { DcLoadingHandleViewModel(get(), get()) }
    viewModel { DcLoadingBoxesViewModel(get(), get(), get(), get()) }
    viewModel { (parameters: DcLoadingBoxNotBelongParameters) ->
        DcLoadingBoxNotBelongViewModel(parameters)
    }

    viewModel { FlightLoaderViewModel(get(), get()) }
    viewModel { FlightsViewModel(get(), get(), get()) }
    viewModel { FlightPickPointViewModel(get(), get(), get(), get()) }
    viewModel { FlightDeliveriesViewModel(get(), get(), get(), get()) }
    viewModel { (parameters: FlightDeliveriesDetailsParameters) ->
        FlightDeliveriesDetailsViewModel(parameters, get(), get(), get())
    }

    viewModel { (parameters: UnloadingScanParameters) ->
        UnloadingScanViewModel(parameters, get(), get(), get())
    }
    viewModel { (parameters: UnloadingBoxNotBelongParameters) ->
        UnloadingBoxNotBelongModel(parameters)
    }
    viewModel { (parameters: UnloadingBoxesParameters) ->
        UnloadingBoxesViewModel(parameters, get(), get(), get(), get())
    }
    viewModel { (parameters: UnloadingReturnParameters) ->
        UnloadingReturnBoxesViewModel(parameters, get(), get(), get(), get())
    }
    viewModel { (parameters: UnloadingHandleParameters) ->
        UnloadingHandleViewModel(parameters, get(), get(), get(), get())
    }

    viewModel { (parameters: ForcedTerminationParameters) ->
        ForcedTerminationViewModel(parameters, get(), get(), get(), get())
    }

    viewModel {
        CongratulationViewModel(get(), get(), get())
    }

    viewModel {
        DcUnloadingScanViewModel(get(), get(), get())
    }
    viewModel {
        DcUnloadingHandleViewModel(get(), get(), get(), get())
    }
    viewModel { (parameters: DcUnloadingBoxNotBelongParameters) ->
        DcUnloadingBoxNotBelongModel(parameters)
    }
    viewModel { DcUnloadingBoxesViewModel(get(), get(), get(), get()) }
    viewModel { DcForcedTerminationViewModel(get(), get(), get()) }
    viewModel { DcForcedTerminationDetailsViewModel(get(), get(), get()) }
    viewModel { DcUnloadingCongratulationViewModel(get(), get(), get(), get()) }

}