package ru.wb.go.di.module

import android.app.Application
import org.koin.dsl.module
import ru.wb.go.ui.auth.AuthResourceProvider
import ru.wb.go.ui.courierbilling.CourierBillingResourceProvider
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResourceProvider
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryResourceProvider
import ru.wb.go.ui.courierdata.CourierDataResourceProvider
import ru.wb.go.ui.courierexpects.CourierExpectsResourceProvider
import ru.wb.go.ui.courierintransit.CourierIntransitResourceProvider
import ru.wb.go.ui.courierloader.CourierLoaderResourceProvider
import ru.wb.go.ui.courierloading.CourierLoadingResourceProvider
import ru.wb.go.ui.couriermap.CourierMapResourceProvider
import ru.wb.go.ui.courierorderconfirm.CourierOrderConfirmResourceProvider
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsResourceProvider
import ru.wb.go.ui.courierorders.CourierOrdersResourceProvider
import ru.wb.go.ui.courierordertimer.CourierOrderTimerResourceProvider
import ru.wb.go.ui.courierstartdelivery.CourierStartDeliveryResourceProvider
import ru.wb.go.ui.courierunloading.CourierUnloadingResourceProvider
import ru.wb.go.ui.courierversioncontrol.CourierVersionControlResourceProvider
import ru.wb.go.ui.courierwarehouses.CourierWarehousesResourceProvider
import ru.wb.go.ui.dcloading.DcLoadingResourceProvider
import ru.wb.go.ui.dcunloading.DcUnloadingScanResourceProvider
import ru.wb.go.ui.dcunloadingcongratulation.DcUnloadingCongratulationResourceProvider
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsResourceProvider
import ru.wb.go.ui.dcunloadingforcedtermination.DcForcedTerminationResourceProvider
import ru.wb.go.ui.flightdeliveries.FlightDeliveriesResourceProvider
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsResourceProvider
import ru.wb.go.ui.flightpickpoint.FlightPickPointResourceProvider
import ru.wb.go.ui.flights.FlightsResourceProvider
import ru.wb.go.ui.flightsloader.FlightLoaderProvider
import ru.wb.go.ui.splash.AppResourceProvider
import ru.wb.go.ui.unloadingcongratulation.CongratulationResourceProvider
import ru.wb.go.ui.unloadingforcedtermination.ForcedTerminationResourceProvider
import ru.wb.go.ui.unloadingscan.UnloadingScanResourceProvider

val resourceModule = module {

    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideUserDataResourceProvider(application: Application): CourierDataResourceProvider {
        return CourierDataResourceProvider(application)
    }

    fun provideCouriersCompleteRegistrationResourceProvider(application: Application): CourierExpectsResourceProvider {
        return CourierExpectsResourceProvider(application)
    }

    fun provideCourierVersionControlResourceProvider(application: Application): CourierVersionControlResourceProvider {
        return CourierVersionControlResourceProvider(application)
    }

    fun provideCourierOrderDetailsResourceProvider(application: Application): CourierOrderDetailsResourceProvider {
        return CourierOrderDetailsResourceProvider(application)
    }

    fun provideCourierWarehouseResourceProvider(application: Application): CourierWarehousesResourceProvider {
        return CourierWarehousesResourceProvider(application)
    }

    fun provideCourierOrderResourceProvider(application: Application): CourierOrdersResourceProvider {
        return CourierOrdersResourceProvider(application)
    }

    fun provideCourierCarNumberResourceProvider(application: Application): CourierCarNumberResourceProvider {
        return CourierCarNumberResourceProvider(application)
    }

    fun provideCourierOrderConfirmResourceProvider(application: Application): CourierOrderConfirmResourceProvider {
        return CourierOrderConfirmResourceProvider(application)
    }

    fun provideCourierLoaderResourceProvider(application: Application): CourierLoaderResourceProvider {
        return CourierLoaderResourceProvider(application)
    }

    fun provideCourierOrderTimerResourceProvider(application: Application): CourierOrderTimerResourceProvider {
        return CourierOrderTimerResourceProvider(application)
    }

    fun provideCourierBillingResourceProvider(application: Application): CourierBillingResourceProvider {
        return CourierBillingResourceProvider(application)
    }


    fun provideCourierScannerLoadingResourceProvider(application: Application): CourierLoadingResourceProvider {
        return CourierLoadingResourceProvider(application)
    }

    fun provideCourierIntransitResourceProvider(application: Application): CourierIntransitResourceProvider {
        return CourierIntransitResourceProvider(application)
    }

    fun provideCourierUnloadingResourceProvider(application: Application): CourierUnloadingResourceProvider {
        return CourierUnloadingResourceProvider(application)
    }

    fun provideCourierCompleteDeliveryResourceProvider(application: Application): CourierCompleteDeliveryResourceProvider {
        return CourierCompleteDeliveryResourceProvider(application)
    }

    fun provideCourierStartDeliveryResourceProvider(application: Application): CourierStartDeliveryResourceProvider {
        return CourierStartDeliveryResourceProvider(application)
    }

    fun provideCourierMapResourceProvider(application: Application): CourierMapResourceProvider {
        return CourierMapResourceProvider(application)
    }

    fun provideFlightLoaderProvider(application: Application): FlightLoaderProvider {
        return FlightLoaderProvider(application)
    }

    fun provideFlightResourceProvider(application: Application): FlightsResourceProvider {
        return FlightsResourceProvider(application)
    }

    fun provideReceptionResourceProvider(application: Application): DcLoadingResourceProvider {
        return DcLoadingResourceProvider(application)
    }

    fun provideFlightPickPointResourceProvider(application: Application): FlightPickPointResourceProvider {
        return FlightPickPointResourceProvider(application)
    }

    fun provideFlightDeliveriesResourceProvider(application: Application): FlightDeliveriesResourceProvider {
        return FlightDeliveriesResourceProvider(application)
    }

    fun provideFlightDeliveriesDetailsResourceProvider(application: Application): FlightDeliveriesDetailsResourceProvider {
        return FlightDeliveriesDetailsResourceProvider(application)
    }

    fun provideTemporaryPasswordResourceProvider(application: Application): AuthResourceProvider {
        return AuthResourceProvider(application)
    }

    fun provideUnloadingScanResourceProvider(application: Application): UnloadingScanResourceProvider {
        return UnloadingScanResourceProvider(application)
    }

    fun provideForcedTerminationResourceProvider(application: Application): ForcedTerminationResourceProvider {
        return ForcedTerminationResourceProvider(application)
    }

    fun provideCongratulationResourceProvider(application: Application): CongratulationResourceProvider {
        return CongratulationResourceProvider(application)
    }

    fun provideDcUnloadingScanResourceProvider(application: Application): DcUnloadingScanResourceProvider {
        return DcUnloadingScanResourceProvider(application)
    }

    fun provideDcForcedTerminationResourceProvider(application: Application): DcForcedTerminationResourceProvider {
        return DcForcedTerminationResourceProvider(application)
    }

    fun provideDcUnloadingCongratulationResourceProvider(application: Application): DcUnloadingCongratulationResourceProvider {
        return DcUnloadingCongratulationResourceProvider(application)
    }

    fun provideDcForcedTerminationDetailsResourceProvider(application: Application): DcForcedTerminationDetailsResourceProvider {
        return DcForcedTerminationDetailsResourceProvider(application)
    }

    single { provideAppResourceProvider(get()) }
    single { provideUserDataResourceProvider(get()) }
    single { provideCouriersCompleteRegistrationResourceProvider(get()) }
    single { provideCourierVersionControlResourceProvider(get()) }
    single { provideCourierOrderDetailsResourceProvider(get()) }
    single { provideCourierWarehouseResourceProvider(get()) }
    single { provideCourierOrderResourceProvider(get()) }
    single { provideCourierCarNumberResourceProvider(get()) }
    single { provideCourierOrderConfirmResourceProvider(get()) }
    single { provideCourierLoaderResourceProvider(get()) }
    single { provideCourierOrderTimerResourceProvider(get()) }
    single { provideCourierScannerLoadingResourceProvider(get()) }
    single { provideCourierIntransitResourceProvider(get()) }
    single { provideCourierUnloadingResourceProvider(get()) }
    single { provideCourierCompleteDeliveryResourceProvider(get()) }
    single { provideCourierStartDeliveryResourceProvider(get()) }
    single { provideCourierMapResourceProvider(get()) }
    single { provideCourierBillingResourceProvider(get()) }

    single { provideFlightLoaderProvider(get()) }
    single { provideFlightResourceProvider(get()) }
    single { provideReceptionResourceProvider(get()) }
    single { provideFlightPickPointResourceProvider(get()) }
    single { provideFlightDeliveriesResourceProvider(get()) }
    single { provideFlightDeliveriesDetailsResourceProvider(get()) }
    single { provideTemporaryPasswordResourceProvider(get()) }
    single { provideUnloadingScanResourceProvider(get()) }
    single { provideForcedTerminationResourceProvider(get()) }
    single { provideCongratulationResourceProvider(get()) }
    single { provideDcUnloadingScanResourceProvider(get()) }
    single { provideDcForcedTerminationResourceProvider(get()) }
    single { provideDcForcedTerminationDetailsResourceProvider(get()) }
    single { provideDcUnloadingCongratulationResourceProvider(get()) }

}