package ru.wb.perevozka.di.module

import android.app.Application
import org.koin.dsl.module
import ru.wb.perevozka.ui.auth.AuthResourceProvider
import ru.wb.perevozka.ui.courierorders.CourierOrderResourceProvider
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
import ru.wb.perevozka.ui.userdata.UserDataResourceProvider
import ru.wb.perevozka.ui.userdata.couriers.CouriersCompleteRegistrationResourceProvider

val resourceModule = module {

    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideUserDataResourceProvider(application: Application): UserDataResourceProvider {
        return UserDataResourceProvider(application)
    }

    fun provideCouriersCompleteRegistrationResourceProvider(application: Application): CouriersCompleteRegistrationResourceProvider {
        return CouriersCompleteRegistrationResourceProvider(application)
    }

    fun provideCourierWarehouseResourceProvider(application: Application): CourierWarehousesResourceProvider {
        return CourierWarehousesResourceProvider(application)
    }

    fun provideCourierOrderResourceProvider(application: Application): CourierOrderResourceProvider {
        return CourierOrderResourceProvider(application)
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
    single { provideCourierWarehouseResourceProvider(get()) }
    single { provideCourierOrderResourceProvider(get()) }
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