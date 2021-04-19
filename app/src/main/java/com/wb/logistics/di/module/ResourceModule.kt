package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.ui.auth.AuthResourceProvider
import com.wb.logistics.ui.flightdeliveries.FlightDeliveriesResourceProvider
import com.wb.logistics.ui.flights.FlightsResourceProvider
import com.wb.logistics.ui.reception.ReceptionResourceProvider
import com.wb.logistics.ui.res.AppResourceProvider
import org.koin.dsl.module

val resourceModule = module {
    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideFlightResourceProvider(application: Application): FlightsResourceProvider {
        return FlightsResourceProvider(application)
    }

    fun provideReceptionResourceProvider(application: Application): ReceptionResourceProvider {
        return ReceptionResourceProvider(application)
    }

    fun provideFlightDeliveriesResourceProvider(application: Application): FlightDeliveriesResourceProvider {
        return FlightDeliveriesResourceProvider(application)
    }

    fun provideTemporaryPasswordResourceProvider(application: Application): AuthResourceProvider {
        return AuthResourceProvider(application)
    }

    single { provideAppResourceProvider(get()) }
    single { provideFlightResourceProvider(get()) }
    single { provideReceptionResourceProvider(get()) }
    single { provideFlightDeliveriesResourceProvider(get()) }
    single { provideTemporaryPasswordResourceProvider(get()) }
}