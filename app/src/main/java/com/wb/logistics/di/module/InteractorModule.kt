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
import com.wb.logistics.ui.dcloading.domain.DcLoadingInteractor
import com.wb.logistics.ui.dcloading.domain.DcLoadingInteractorImpl
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractorImpl
import com.wb.logistics.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import com.wb.logistics.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractorImpl
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractorImpl
import com.wb.logistics.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractor
import com.wb.logistics.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractorImpl
import com.wb.logistics.ui.flightloader.domain.FlightsLoaderInteractor
import com.wb.logistics.ui.flightloader.domain.FlightsLoaderInteractorImpl
import com.wb.logistics.ui.flightpickpoint.domain.FlightPickPointInteractor
import com.wb.logistics.ui.flightpickpoint.domain.FlightPickPointInteractorImpl
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.ui.flights.domain.FlightsInteractorImpl
import com.wb.logistics.ui.forcedtermination.domain.ForcedTerminationInteractor
import com.wb.logistics.ui.forcedtermination.domain.ForcedTerminationInteractorImpl
import com.wb.logistics.ui.scanner.domain.ScannerInteractor
import com.wb.logistics.ui.scanner.domain.ScannerInteractorImpl
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import com.wb.logistics.ui.splash.domain.AppInteractor
import com.wb.logistics.ui.splash.domain.AppInteractorImpl
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.ui.unloading.domain.UnloadingInteractorImpl
import com.wb.logistics.ui.unloadingcongratulation.domain.CongratulationInteractorImpl
import com.wb.logistics.utils.managers.ScreenManager
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
        authRemoteRepository: AuthRemoteRepository,
        appLocalRepository: AppLocalRepository,
    ): AppInteractor {
        return AppInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            authRemoteRepository,
            appLocalRepository)
    }

    fun provideFlightsLoaderInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        authRemoteRepository: AuthRemoteRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): FlightsLoaderInteractor {
        return FlightsLoaderInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            authRemoteRepository,
            timeManager,
            screenManager)
    }

    fun provideFlightsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
    ): FlightsInteractor {
        return FlightsInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository)
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
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        screenManager: ScreenManager,
    ): FlightPickPointInteractor {
        return FlightPickPointInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            screenManager)
    }

    fun provideFlightDeliveriesInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        screenManager: ScreenManager,
    ): FlightDeliveriesInteractor {
        return FlightDeliveriesInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            screenManager)
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
        screenManager: ScreenManager,
    ): DcLoadingInteractor {
        return DcLoadingInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager)
    }

    fun provideUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): UnloadingInteractor {
        return UnloadingInteractorImpl(rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager)
    }

    fun provideForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
        screenManager: ScreenManager,
    ): ForcedTerminationInteractor {
        return ForcedTerminationInteractorImpl(rxSchedulerFactory,
            appLocalRepository,
            screenManager)
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
        screenManager: ScreenManager,
    ): DcForcedTerminationInteractor {
        return DcForcedTerminationInteractorImpl(rxSchedulerFactory,
            appLocalRepository,
            screenManager)
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
    single { provideNavigationInteractor(get(), get(), get(), get()) }
    single { provideFlightsLoaderInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideFlightsInteractor(get(), get(), get()) }
    single { provideFlightDeliveriesDetailsInteractor(get(), get(), get()) }
    single { provideFlightPickPointInteractor(get(), get(), get(), get()) }
    single { provideFlightDeliveriesInteractor(get(), get(), get(), get()) }
    single { provideScannerInteractor(get(), get()) }
    single { provideReceptionInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideUnloadingInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideForcedTerminationInteractor(get(), get(), get()) }
    single { provideCongratulationInteractor(get(), get()) }
    single { provideDcUnloadingInteractor(get(), get(), get(), get(), get()) }
    single { provideDcForcedTerminationInteractor(get(), get(), get()) }
    single { provideDcUnloadingCongratulationInteractor(get(), get()) }

}