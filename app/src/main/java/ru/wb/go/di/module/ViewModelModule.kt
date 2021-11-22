package ru.wb.go.di.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.wb.go.ui.auth.AuthLoaderViewModel
import ru.wb.go.ui.auth.CheckSmsParameters
import ru.wb.go.ui.auth.CheckSmsViewModel
import ru.wb.go.ui.auth.NumberPhoneViewModel
import ru.wb.go.ui.config.ConfigViewModel
import ru.wb.go.ui.courieragreement.CourierAgreementViewModel
import ru.wb.go.ui.courierbilling.CourierBillingViewModel
import ru.wb.go.ui.couriercarnumber.CourierCarNumberViewModel
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryParameters
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryViewModel
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.ui.courierdata.UserFormViewModel
import ru.wb.go.ui.courierexpects.CourierExpectsParameters
import ru.wb.go.ui.courierexpects.CouriersCompleteRegistrationViewModel
import ru.wb.go.ui.courierintransit.CourierIntransitViewModel
import ru.wb.go.ui.courierloader.CourierLoaderViewModel
import ru.wb.go.ui.courierloading.CourierLoadingBoxesViewModel
import ru.wb.go.ui.courierloading.CourierLoadingScanViewModel
import ru.wb.go.ui.courierloading.CourierLoadingUnknownBoxViewModel
import ru.wb.go.ui.couriermap.CourierMapViewModel
import ru.wb.go.ui.courierorderconfirm.CourierOrderConfirmViewModel
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsParameters
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsViewModel
import ru.wb.go.ui.courierorders.CourierOrderParameters
import ru.wb.go.ui.courierorders.CourierOrdersViewModel
import ru.wb.go.ui.courierordertimer.CourierOrderTimerViewModel
import ru.wb.go.ui.courierstartdelivery.CourierStartDeliveryParameters
import ru.wb.go.ui.courierstartdelivery.CourierStartDeliveryViewModel
import ru.wb.go.ui.courierunloading.CourierUnloadingScanParameters
import ru.wb.go.ui.courierunloading.CourierUnloadingScanViewModel
import ru.wb.go.ui.courierunloading.CourierUnloadingUnknownBoxViewModel
import ru.wb.go.ui.courierversioncontrol.CourierVersionControlViewModel
import ru.wb.go.ui.courierwarehouses.CourierWarehousesViewModel
import ru.wb.go.ui.dcloading.*
import ru.wb.go.ui.dcunloading.*
import ru.wb.go.ui.dcunloadingcongratulation.DcUnloadingCongratulationViewModel
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsViewModel
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationViewModel
import ru.wb.go.ui.flightdeliveries.FlightDeliveriesViewModel
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsViewModel
import ru.wb.go.ui.flightpickpoint.FlightPickPointViewModel
import ru.wb.go.ui.flights.FlightsViewModel
import ru.wb.go.ui.flightsloader.FlightLoaderViewModel
import ru.wb.go.ui.scanner.CourierScannerViewModel
import ru.wb.go.ui.scanner.ScannerViewModel
import ru.wb.go.ui.splash.AppLoaderViewModel
import ru.wb.go.ui.splash.AppViewModel
import ru.wb.go.ui.unloadingboxes.UnloadingBoxesParameters
import ru.wb.go.ui.unloadingboxes.UnloadingBoxesViewModel
import ru.wb.go.ui.unloadingcongratulation.CongratulationViewModel
import ru.wb.go.ui.unloadingforcedtermination.ForcedTerminationParameters
import ru.wb.go.ui.unloadingforcedtermination.ForcedTerminationViewModel
import ru.wb.go.ui.unloadinghandle.UnloadingHandleParameters
import ru.wb.go.ui.unloadinghandle.UnloadingHandleViewModel
import ru.wb.go.ui.unloadingreturnboxes.UnloadingReturnBoxesViewModel
import ru.wb.go.ui.unloadingreturnboxes.UnloadingReturnParameters
import ru.wb.go.ui.unloadingscan.UnloadingBoxNotBelongModel
import ru.wb.go.ui.unloadingscan.UnloadingBoxNotBelongParameters
import ru.wb.go.ui.unloadingscan.UnloadingScanParameters
import ru.wb.go.ui.unloadingscan.UnloadingScanViewModel

val viewModelModule = module {

    viewModel { AppLoaderViewModel(get(), get(), get(), get()) }
    viewModel { AuthLoaderViewModel(get()) }
    viewModel { AppViewModel(get(), get(), get(), get()) }

    viewModel { NumberPhoneViewModel(get(), get(), get()) }
    viewModel { (parameters: CheckSmsParameters) ->
        CheckSmsViewModel(parameters, get(), get(), get())
    }

    viewModel {
        CourierLoaderViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { CourierVersionControlViewModel(get(), get(), get(), get()) }
    viewModel { ConfigViewModel(get(), get()) }
    viewModel { CourierAgreementViewModel(get()) }

    viewModel { (parameters: CourierDataParameters) ->
        UserFormViewModel(parameters, get(), get(), get())
    }
    viewModel { (parameters: CourierExpectsParameters) ->
        CouriersCompleteRegistrationViewModel(
            parameters,
            get(),
            get(),
            get()
        )
    }

    viewModel { (parameters: CourierOrderDetailsParameters) ->
        CourierOrderDetailsViewModel(
            parameters,
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { CourierCarNumberViewModel(get(), get(), get()) }

    viewModel { CourierOrderConfirmViewModel(get(), get(), get(), get()) }

    viewModel { CourierOrderTimerViewModel(get(), get(), get()) }

    viewModel { CourierLoadingUnknownBoxViewModel() }

    viewModel {
        CourierLoadingBoxesViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        CourierLoadingScanViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { CourierScannerViewModel(get(), get()) }
    viewModel { CourierIntransitViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { (parameters: CourierUnloadingScanParameters) ->
        CourierUnloadingScanViewModel(
            parameters,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { CourierUnloadingUnknownBoxViewModel() }
    viewModel { (parameters: CourierStartDeliveryParameters) ->
        CourierStartDeliveryViewModel(
            parameters,
            get(),
            get(),
            get()
        )
    }
    viewModel { (parameters: CourierCompleteDeliveryParameters) ->
        CourierCompleteDeliveryViewModel(
            parameters,
            get(),
            get(),
            get()
        )
    }
    viewModel { CourierMapViewModel(get(), get(), get()) }


    viewModel { ScannerViewModel(get(), get()) }

    viewModel { DcLoadingScanViewModel(get(), get(), get()) }
    viewModel { DcLoadingHandleViewModel(get(), get()) }
    viewModel { DcLoadingBoxesViewModel(get(), get(), get(), get()) }

    viewModel { CourierWarehousesViewModel(get(), get(), get()) }

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

    viewModel { (parameters: CourierOrderParameters) ->
        CourierOrdersViewModel(parameters, get(), get(), get(), get(), get())
    }

    viewModel { CourierBillingViewModel(get(), get(), get(), get(), get()) }

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

    viewModel { CongratulationViewModel(get(), get(), get()) }

    viewModel { DcUnloadingScanViewModel(get(), get(), get()) }
    viewModel { DcUnloadingHandleViewModel(get(), get(), get(), get()) }
    viewModel { (parameters: DcUnloadingBoxNotBelongParameters) ->
        DcUnloadingBoxNotBelongModel(parameters)
    }
    viewModel { DcUnloadingBoxesViewModel(get(), get(), get(), get()) }
    viewModel { DcForcedTerminationViewModel(get(), get(), get()) }
    viewModel { DcForcedTerminationDetailsViewModel(get(), get(), get()) }
    viewModel { DcUnloadingCongratulationViewModel(get(), get(), get(), get()) }

}