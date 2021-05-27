package com.wb.logistics.di.module

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.ui.auth.domain.*
import com.wb.logistics.ui.dcforcedtermination.domain.DcForcedTerminationInteractor
import com.wb.logistics.ui.dcforcedtermination.domain.DcForcedTerminationInteractorImpl
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractorImpl
import com.wb.logistics.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import com.wb.logistics.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractorImpl
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractorImpl
import com.wb.logistics.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractor
import com.wb.logistics.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractorImpl
import com.wb.logistics.ui.flightpickpoint.domain.FlightPickPointInteractor
import com.wb.logistics.ui.flightpickpoint.domain.FlightPickPointInteractorImpl
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.ui.flights.domain.FlightsInteractorImpl
import com.wb.logistics.ui.forcedtermination.domain.ForcedTerminationInteractor
import com.wb.logistics.ui.forcedtermination.domain.ForcedTerminationInteractorImpl
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.ui.reception.domain.ReceptionInteractorImpl
import com.wb.logistics.ui.scanner.domain.ScannerInteractor
import com.wb.logistics.ui.scanner.domain.ScannerInteractorImpl
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import com.wb.logistics.ui.splash.domain.NavigationInteractor
import com.wb.logistics.ui.splash.domain.NavigationInteractorImpl
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.ui.unloading.domain.UnloadingInteractorImpl
import com.wb.logistics.ui.unloadingcongratulation.domain.CongratulationInteractorImpl
import org.koin.dsl.module
import com.wb.logistics.ui.unloadingcongratulation.domain.CongratulationInteractor as CongratulationInteractor1

val interactorModule = module {

    fun provideTemporaryPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRemoteRepository,
    ): TemporaryPasswordInteractor {
        return TemporaryPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideInputPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        authRepository: AuthRemoteRepository,
    ): InputPasswordInteractor {
        return InputPasswordInteractorImpl(rxSchedulerFactory, authRepository)
    }

    fun provideCreatePasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRemoteRepository,
    ): CreatePasswordInteractor {
        return CreatePasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideNavigationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        authRepository: AuthRemoteRepository,
    ): NavigationInteractor {
        return NavigationInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            authRepository)
    }

    fun provideFlightsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        timeManager: TimeManager,
    ): FlightsInteractor {
        return FlightsInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            timeManager)
    }

    fun provideFlightDeliveriesDetailsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appLocalRepository: AppLocalRepository,
    ): FlightDeliveriesDetailsInteractor {
        return FlightDeliveriesDetailsInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appLocalRepository)
    }

    fun provideFlightPickPointInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appLocalRepository: AppLocalRepository,
    ): FlightPickPointInteractor {
        return FlightPickPointInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appLocalRepository)
    }

    fun provideFlightDeliveriesInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appLocalRepository: AppLocalRepository,
    ): FlightDeliveriesInteractor {
        return FlightDeliveriesInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appLocalRepository)
    }

    fun provideScannerInteractor(
        rxSchedulerFactory: RxSchedulerFactory, scannerRepository: ScannerRepository,
    ): ScannerInteractor {
        return ScannerInteractorImpl(rxSchedulerFactory, scannerRepository)
    }

    fun provideReceptionInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
    ): ReceptionInteractor {
        return ReceptionInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager)
    }

    fun provideUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
    ): UnloadingInteractor {
        return UnloadingInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager)
    }

    fun provideForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): ForcedTerminationInteractor {
        return ForcedTerminationInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideCongratulationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): CongratulationInteractor1 {
        return CongratulationInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideDcUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
    ): DcUnloadingInteractor {
        return DcUnloadingInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager)
    }

    fun provideDcForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): DcForcedTerminationInteractor {
        return DcForcedTerminationInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideDcUnloadingCongratulationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): DcUnloadingCongratulationInteractor {
        return DcUnloadingCongratulationInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    single { provideTemporaryPasswordInteractor(get(), get()) }
    single { provideInputPasswordInteractor(get(), get()) }
    single { provideCreatePasswordInteractor(get(), get()) }
    single { provideNavigationInteractor(get(), get(), get()) }
    single { provideFlightsInteractor(get(), get(), get(), get(), get()) }
    single { provideFlightDeliveriesDetailsInteractor(get(), get(), get()) }
    single { provideFlightPickPointInteractor(get(), get(), get()) }
    single { provideFlightDeliveriesInteractor(get(), get(), get()) }
    single { provideScannerInteractor(get(), get()) }
    single { provideReceptionInteractor(get(), get(), get(), get(), get()) }
    single { provideUnloadingInteractor(get(), get(), get(), get(), get()) }
    single { provideForcedTerminationInteractor(get(), get()) }
    single { provideCongratulationInteractor(get(), get()) }
    single { provideDcUnloadingInteractor(get(), get(), get(), get(), get()) }
    single { provideDcForcedTerminationInteractor(get(), get()) }
    single { provideDcUnloadingCongratulationInteractor(get(), get()) }

}