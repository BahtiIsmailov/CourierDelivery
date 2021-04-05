package com.wb.logistics.di.module

import com.wb.logistics.data.AppRepository
import com.wb.logistics.network.api.BoxesRepository
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.auth.domain.*
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.ui.flights.domain.FlightsInteractorImpl
import com.wb.logistics.ui.nav.domain.NavigationInteractor
import com.wb.logistics.ui.nav.domain.NavigationInteractorImpl
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.ui.reception.domain.ReceptionInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    fun provideTemporaryPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository,
    ): TemporaryPasswordInteractor {
        return TemporaryPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideInputPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository,
    ): InputPasswordInteractor {
        return InputPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideCreatePasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository,
    ): CreatePasswordInteractor {
        return CreatePasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideNavigationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        authRepository: AuthRepository,
    ): NavigationInteractor {
        return NavigationInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            authRepository
        )
    }

    fun provideFlightsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRepository: AppRepository,
        boxesRepository: BoxesRepository,
    ): FlightsInteractor {
        return FlightsInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appRepository,
            boxesRepository)
    }

    fun provideReceptionInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        boxesRepository: BoxesRepository,
    ): ReceptionInteractor {
        return ReceptionInteractorImpl(rxSchedulerFactory, boxesRepository)
    }

    single { provideTemporaryPasswordInteractor(get(), get()) }
    single { provideInputPasswordInteractor(get(), get()) }
    single { provideCreatePasswordInteractor(get(), get()) }
    single { provideNavigationInteractor(get(), get(), get()) }
    single { provideFlightsInteractor(get(), get(), get(), get()) }
    single { provideReceptionInteractor(get(), get()) }

}