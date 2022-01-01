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
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataAmountParameters
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataViewModel
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorAmountParameters
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorViewModel
import ru.wb.go.ui.courierbilllingcomplete.CourierBillingCompleteParameters
import ru.wb.go.ui.courierbilllingcomplete.CourierBillingCompleteViewModel
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
import ru.wb.go.ui.scanner.CourierScannerViewModel
import ru.wb.go.ui.scanner.ScannerViewModel
import ru.wb.go.ui.splash.AppLoaderViewModel
import ru.wb.go.ui.splash.AppViewModel

val viewModelModule = module {
    viewModel { ConfigViewModel(get(), get()) }

    viewModel { AppLoaderViewModel(get(), get(), get(), get(), get()) }
    viewModel { AuthLoaderViewModel(get(), get()) }
    viewModel { AppViewModel(get(), get(), get(), get(), get()) }

    viewModel { NumberPhoneViewModel(get(), get(), get(), get()) }
    viewModel { (parameters: CheckSmsParameters) ->
        CheckSmsViewModel(parameters, get(), get(), get(), get())
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
            get()
        )
    }
    viewModel { CourierVersionControlViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CourierAgreementViewModel(get(), get()) }

    viewModel { CourierWarehousesViewModel(get(), get(), get(), get()) }

    viewModel { (parameters: CourierOrderParameters) ->
        CourierOrdersViewModel(parameters, get(), get(), get(), get(), get(), get())
    }

    viewModel { (parameters: CourierDataParameters) ->
        UserFormViewModel(parameters, get(), get(), get(), get())
    }
    viewModel { (parameters: CourierExpectsParameters) ->
        CouriersCompleteRegistrationViewModel(parameters, get(), get(), get(), get(), get(), get())
    }

    viewModel { (parameters: CourierOrderDetailsParameters) ->
        CourierOrderDetailsViewModel(parameters, get(), get(), get(), get(), get())
    }

    viewModel { CourierCarNumberViewModel(get(), get(), get(), get()) }

    viewModel { CourierOrderConfirmViewModel(get(), get(), get(), get(), get()) }

    viewModel { CourierOrderTimerViewModel(get(), get(), get(), get()) }

    viewModel { CourierLoadingUnknownBoxViewModel() }

    viewModel {
        CourierLoadingBoxesViewModel(
            get(),
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
            get(),
            get()
        )
    }

    viewModel { CourierScannerViewModel(get(), get(), get()) }
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
        CourierStartDeliveryViewModel(parameters, get(), get(), get(), get())
    }
    viewModel { (parameters: CourierCompleteDeliveryParameters) ->
        CourierCompleteDeliveryViewModel(parameters, get(), get(), get(), get())
    }
    viewModel { CourierMapViewModel(get(), get(), get(), get()) }

    viewModel { ScannerViewModel(get(), get(), get()) }

    viewModel { CourierBillingViewModel(get(), get(), get(), get(), get(), get(), get()) }

    viewModel { (parameters: CourierBillingAccountDataAmountParameters) ->
        CourierBillingAccountDataViewModel(
            parameters,
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { (parameters: CourierBillingAccountSelectorAmountParameters) ->
        CourierBillingAccountSelectorViewModel(
            parameters,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { (parameters: CourierBillingCompleteParameters) ->
        CourierBillingCompleteViewModel(
                parameters,
                get(),
                get(),
                get(),
                get()
        )
    }

}