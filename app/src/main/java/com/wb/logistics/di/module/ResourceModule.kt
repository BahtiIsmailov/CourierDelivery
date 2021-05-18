package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.ui.auth.AuthResourceProvider
import com.wb.logistics.ui.congratulation.CongratulationResourceProvider
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesResourceProvider
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsResourceProvider
import com.wb.logistics.ui.flightpickpoint.FlightPickPointResourceProvider
import com.wb.logistics.ui.flights.FlightsResourceProvider
import com.wb.logistics.ui.forcedtermination.ForcedTerminationResourceProvider
import com.wb.logistics.ui.nav.NavigationResourceProvider
import com.wb.logistics.ui.reception.ReceptionScanResourceProvider
import com.wb.logistics.ui.scanner.ScannerResourceProvider
import com.wb.logistics.ui.unloading.UnloadingScanResourceProvider
import org.koin.dsl.module

val resourceModule = module {

    fun provideAppResourceProvider(application: Application): NavigationResourceProvider {
        return NavigationResourceProvider(application)
    }

    fun provideFlightResourceProvider(application: Application): FlightsResourceProvider {
        return FlightsResourceProvider(application)
    }

    fun provideReceptionResourceProvider(application: Application): ReceptionScanResourceProvider {
        return ReceptionScanResourceProvider(application)
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

    single { provideAppResourceProvider(get()) }
    single { provideFlightResourceProvider(get()) }
    single { provideReceptionResourceProvider(get()) }
    single { provideFlightPickPointResourceProvider(get()) }
    single { provideFlightDeliveriesResourceProvider(get()) }
    single { provideFlightDeliveriesDetailsResourceProvider(get()) }
    single { provideTemporaryPasswordResourceProvider(get()) }
    single { provideScannerResourceProvider(get()) }
    single { provideUnloadingScanResourceProvider(get()) }
    single { provideForcedTerminationResourceProvider(get()) }
    single { provideCongratulationResourceProvider(get()) }

}