package ru.wb.perevozka.di.module

import android.app.Application
import org.koin.dsl.module
import ru.wb.perevozka.ui.auth.AuthResourceProvider
import ru.wb.perevozka.ui.courierorders.CourierOrdersResourceProvider
import ru.wb.perevozka.ui.courierwarehouses.CourierWarehousesResourceProvider
import ru.wb.perevozka.ui.dcloading.DcLoadingResourceProvider
import ru.wb.perevozka.ui.dcunloading.DcUnloadingScanResourceProvider
import ru.wb.perevozka.ui.dcunloadingcongratulation.DcUnloadingCongratulationResourceProvider
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsResourceProvider
import ru.wb.perevozka.ui.dcunloadingforcedtermination.DcForcedTerminationResourceProvider
import ru.wb.perevozka.ui.flightdeliveries.FlightDeliveriesResourceProvider
import ru.wb.perevozka.ui.flightdeliveriesdetails.FlightDeliveriesDetailsResourceProvider
import ru.wb.perevozka.ui.flightpickpoint.FlightPickPointResourceProvider
import ru.wb.perevozka.ui.flights.FlightsResourceProvider
import ru.wb.perevozka.ui.flightsloader.FlightLoaderProvider
import ru.wb.perevozka.ui.splash.AppResourceProvider
import ru.wb.perevozka.ui.unloadingcongratulation.CongratulationResourceProvider
import ru.wb.perevozka.ui.unloadingforcedtermination.ForcedTerminationResourceProvider
import ru.wb.perevozka.ui.unloadingscan.UnloadingScanResourceProvider
import ru.wb.perevozka.ui.courierdata.CourierDataResourceProvider
import ru.wb.perevozka.ui.courierexpects.CourierExpectsResourceProvider
import ru.wb.perevozka.ui.couriercarnumber.CourierCarNumberResourceProvider
import ru.wb.perevozka.ui.courierintransit.CourierIntransitResourceProvider
import ru.wb.perevozka.ui.courierorderdetails.CourierOrderDetailsResourceProvider
import ru.wb.perevozka.ui.courierordertimer.CourierOrderTimerResourceProvider
import ru.wb.perevozka.ui.courierloading.CourierLoadingResourceProvider
import ru.wb.perevozka.ui.courierorderconfirm.CourierOrderConfirmResourceProvider

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

    fun provideCourierOrderTimerResourceProvider(application: Application): CourierOrderTimerResourceProvider {
        return CourierOrderTimerResourceProvider(application)
    }

    fun provideCourierScannerLoadingResourceProvider(application: Application): CourierLoadingResourceProvider {
        return CourierLoadingResourceProvider(application)
    }

    fun provideCourierIntransitResourceProvider(application: Application): CourierIntransitResourceProvider {
        return CourierIntransitResourceProvider(application)
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
    single { provideCourierOrderDetailsResourceProvider(get()) }
    single { provideCourierWarehouseResourceProvider(get()) }
    single { provideCourierOrderResourceProvider(get()) }
    single { provideCourierCarNumberResourceProvider(get()) }
    single { provideCourierOrderConfirmResourceProvider(get()) }
    single { provideCourierOrderTimerResourceProvider(get()) }
    single { provideCourierScannerLoadingResourceProvider(get()) }
    single { provideCourierIntransitResourceProvider(get()) }

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