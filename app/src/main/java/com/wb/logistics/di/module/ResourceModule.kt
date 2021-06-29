package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.ui.auth.AuthResourceProvider
import com.wb.logistics.ui.dcloading.DcLoadingResourceProvider
import com.wb.logistics.ui.dcunloading.DcUnloadingScanResourceProvider
import com.wb.logistics.ui.dcunloadingcongratulation.DcUnloadingCongratulationResourceProvider
import com.wb.logistics.ui.dcunloadingforcedtermination.DcForcedTerminationDetailsResourceProvider
import com.wb.logistics.ui.dcunloadingforcedtermination.DcForcedTerminationResourceProvider
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesResourceProvider
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsResourceProvider
import com.wb.logistics.ui.flightloader.FlightLoaderProvider
import com.wb.logistics.ui.flightpickpoint.FlightPickPointResourceProvider
import com.wb.logistics.ui.flights.FlightsResourceProvider
import com.wb.logistics.ui.flightsempty.FlightsEmptyResourceProvider
import com.wb.logistics.ui.scanner.ScannerResourceProvider
import com.wb.logistics.ui.splash.AppResourceProvider
import com.wb.logistics.ui.unloading.UnloadingScanResourceProvider
import com.wb.logistics.ui.unloadingcongratulation.CongratulationResourceProvider
import com.wb.logistics.ui.unloadingforcedtermination.ForcedTerminationResourceProvider
import org.koin.dsl.module

val resourceModule = module {

    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideFlightLoaderProvider(application: Application): FlightLoaderProvider {
        return FlightLoaderProvider(application)
    }

    fun provideFlightResourceProvider(application: Application): FlightsResourceProvider {
        return FlightsResourceProvider(application)
    }

    fun provideFlightEmptyResourceProvider(application: Application): FlightsEmptyResourceProvider {
        return FlightsEmptyResourceProvider(application)
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

    fun provideScannerResourceProvider(application: Application): ScannerResourceProvider {
        return ScannerResourceProvider(application)
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
    single { provideFlightLoaderProvider(get()) }
    single { provideFlightResourceProvider(get()) }
    single { provideFlightEmptyResourceProvider(get()) }
    single { provideReceptionResourceProvider(get()) }
    single { provideFlightPickPointResourceProvider(get()) }
    single { provideFlightDeliveriesResourceProvider(get()) }
    single { provideFlightDeliveriesDetailsResourceProvider(get()) }
    single { provideTemporaryPasswordResourceProvider(get()) }
    single { provideScannerResourceProvider(get()) }
    single { provideUnloadingScanResourceProvider(get()) }
    single { provideForcedTerminationResourceProvider(get()) }
    single { provideCongratulationResourceProvider(get()) }
    single { provideDcUnloadingScanResourceProvider(get()) }
    single { provideDcForcedTerminationResourceProvider(get()) }
    single { provideDcForcedTerminationDetailsResourceProvider(get()) }
    single { provideDcUnloadingCongratulationResourceProvider(get()) }

}