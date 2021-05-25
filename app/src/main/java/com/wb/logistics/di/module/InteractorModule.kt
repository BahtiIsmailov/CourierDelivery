package com.wb.logistics.di.module

import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
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
        repository: AuthRepository,
    ): TemporaryPasswordInteractor {
        return TemporaryPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideInputPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        authRepository: AuthRepository,
    ): InputPasswordInteractor {
        return InputPasswordInteractorImpl(rxSchedulerFactory, authRepository)
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
        return NavigationInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            authRepository)
    }

    fun provideFlightsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRepository: AppRepository,
    ): FlightsInteractor {
        return FlightsInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appRepository)
    }

    fun provideFlightDeliveriesDetailsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRepository: AppRepository,
    ): FlightDeliveriesDetailsInteractor {
        return FlightDeliveriesDetailsInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appRepository)
    }

    fun provideFlightPickPointInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRepository: AppRepository,
    ): FlightPickPointInteractor {
        return FlightPickPointInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appRepository)
    }

    fun provideFlightDeliveriesInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRepository: AppRepository,
    ): FlightDeliveriesInteractor {
        return FlightDeliveriesInteractorImpl(rxSchedulerFactory,
            networkMonitorRepository,
            appRepository)
    }

    fun provideScannerInteractor(
        rxSchedulerFactory: RxSchedulerFactory, scannerRepository: ScannerRepository,
    ): ScannerInteractor {
        return ScannerInteractorImpl(rxSchedulerFactory, scannerRepository)
    }

    fun provideReceptionInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
        scannerRepository: ScannerRepository,
    ): ReceptionInteractor {
        return ReceptionInteractorImpl(rxSchedulerFactory, appRepository, scannerRepository)
    }

    fun provideUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
        scannerRepository: ScannerRepository,
    ): UnloadingInteractor {
        return UnloadingInteractorImpl(rxSchedulerFactory, appRepository, scannerRepository)
    }

    fun provideForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
    ): ForcedTerminationInteractor {
        return ForcedTerminationInteractorImpl(rxSchedulerFactory, appRepository)
    }

    fun provideCongratulationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
    ): CongratulationInteractor1 {
        return CongratulationInteractorImpl(rxSchedulerFactory, appRepository)
    }

    fun provideDcUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
        scannerRepository: ScannerRepository,
    ): DcUnloadingInteractor {
        return DcUnloadingInteractorImpl(rxSchedulerFactory, appRepository, scannerRepository)
    }

    fun provideDcForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
    ): DcForcedTerminationInteractor {
        return DcForcedTerminationInteractorImpl(rxSchedulerFactory, appRepository)
    }

    fun provideDcUnloadingCongratulationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRepository: AppRepository,
    ): DcUnloadingCongratulationInteractor {
        return DcUnloadingCongratulationInteractorImpl(rxSchedulerFactory, appRepository)
    }

    single { provideTemporaryPasswordInteractor(get(), get()) }
    single { provideInputPasswordInteractor(get(), get()) }
    single { provideCreatePasswordInteractor(get(), get()) }
    single { provideNavigationInteractor(get(), get(), get()) }
    single { provideFlightsInteractor(get(), get(), get()) }
    single { provideFlightDeliveriesDetailsInteractor(get(), get(), get()) }
    single { provideFlightPickPointInteractor(get(), get(), get()) }
    single { provideFlightDeliveriesInteractor(get(), get(), get()) }
    single { provideScannerInteractor(get(), get()) }
    single { provideReceptionInteractor(get(), get(), get()) }
    single { provideUnloadingInteractor(get(), get(), get()) }
    single { provideForcedTerminationInteractor(get(), get()) }
    single { provideCongratulationInteractor(get(), get()) }
    single { provideDcUnloadingInteractor(get(), get(), get()) }
    single { provideDcForcedTerminationInteractor(get(), get()) }
    single { provideDcUnloadingCongratulationInteractor(get(), get()) }

}