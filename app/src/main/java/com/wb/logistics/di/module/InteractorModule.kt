package com.wb.logistics.di.module

import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.auth.domain.*
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.ui.flights.domain.FlightsInteractorImpl
import com.wb.logistics.ui.nav.domain.NavigationInteractor
import com.wb.logistics.ui.nav.domain.NavigationInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    fun provideTemporaryPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository
    ): TemporaryPasswordInteractor {
        return TemporaryPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideInputPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository
    ): InputPasswordInteractor {
        return InputPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideCreatePasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository
    ): CreatePasswordInteractor {
        return CreatePasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideNavigationInteractor(
        repository: AuthRepository
    ): NavigationInteractor {
        return NavigationInteractorImpl(repository)
    }

    fun provideFlightsInteractor(
        repository: AppRepository
    ): FlightsInteractor {
        return FlightsInteractorImpl(repository)
    }

    single { provideTemporaryPasswordInteractor(get(), get()) }
    single { provideInputPasswordInteractor(get(), get()) }
    single { provideCreatePasswordInteractor(get(), get()) }
    single { provideNavigationInteractor(get()) }
    single { provideFlightsInteractor(get()) }

}