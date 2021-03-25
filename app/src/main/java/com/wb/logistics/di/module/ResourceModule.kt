package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.ui.auth.AuthResourceProvider
import com.wb.logistics.ui.flights.FlightResourceProvider
import com.wb.logistics.ui.res.AppResourceProvider
import org.koin.dsl.module

val resourceModule = module {
    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideFlightResourceProvider(application: Application): FlightResourceProvider {
        return FlightResourceProvider(application)
    }

    fun provideTemporaryPasswordResourceProvider(application: Application): AuthResourceProvider {
        return AuthResourceProvider(application)
    }

    single { provideAppResourceProvider(get()) }
    single { provideFlightResourceProvider(get()) }
    single { provideTemporaryPasswordResourceProvider(get()) }
}