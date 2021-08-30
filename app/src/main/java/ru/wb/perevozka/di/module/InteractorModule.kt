package ru.wb.perevozka.di.module

import org.koin.dsl.module
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.headers.RefreshTokenRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.auth.domain.CheckSmsInteractor
import ru.wb.perevozka.ui.auth.domain.CheckSmsInteractorImpl
import ru.wb.perevozka.ui.auth.domain.NumberPhoneInteractor
import ru.wb.perevozka.ui.auth.domain.NumberPhoneInteractorImpl
import ru.wb.perevozka.ui.couriercarnumber.domain.CourierCarNumberInteractor
import ru.wb.perevozka.ui.couriercarnumber.domain.CourierCarNumberInteractorImpl
import ru.wb.perevozka.ui.courierdata.domain.CourierDataInteractor
import ru.wb.perevozka.ui.courierdata.domain.CourierDataInteractorImpl
import ru.wb.perevozka.ui.courierexpects.domain.CourierExpectsInteractor
import ru.wb.perevozka.ui.courierexpects.domain.CourierExpectsInteractorImpl
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingInteractor
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingInteractorImpl
import ru.wb.perevozka.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.perevozka.ui.courierorderdetails.domain.CourierOrderDetailsInteractorImpl
import ru.wb.perevozka.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.perevozka.ui.courierorders.domain.CourierOrderInteractorImpl
import ru.wb.perevozka.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.perevozka.ui.courierordertimer.domain.CourierOrderTimerInteractorImpl
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractorImpl
import ru.wb.perevozka.ui.dcloading.domain.DcLoadingInteractor
import ru.wb.perevozka.ui.dcloading.domain.DcLoadingInteractorImpl
import ru.wb.perevozka.ui.dcunloading.domain.DcUnloadingInteractor
import ru.wb.perevozka.ui.dcunloading.domain.DcUnloadingInteractorImpl
import ru.wb.perevozka.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import ru.wb.perevozka.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractorImpl
import ru.wb.perevozka.ui.dcunloadingforcedtermination.domain.DcForcedTerminationInteractor
import ru.wb.perevozka.ui.dcunloadingforcedtermination.domain.DcForcedTerminationInteractorImpl
import ru.wb.perevozka.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import ru.wb.perevozka.ui.flightdeliveries.domain.FlightDeliveriesInteractorImpl
import ru.wb.perevozka.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractor
import ru.wb.perevozka.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractorImpl
import ru.wb.perevozka.ui.flightpickpoint.domain.FlightPickPointInteractor
import ru.wb.perevozka.ui.flightpickpoint.domain.FlightPickPointInteractorImpl
import ru.wb.perevozka.ui.flights.domain.FlightsInteractor
import ru.wb.perevozka.ui.flights.domain.FlightsInteractorImpl
import ru.wb.perevozka.ui.flightsloader.domain.FlightsLoaderInteractor
import ru.wb.perevozka.ui.flightsloader.domain.FlightsLoaderInteractorImpl
import ru.wb.perevozka.ui.scanner.domain.ScannerInteractor
import ru.wb.perevozka.ui.scanner.domain.ScannerInteractorImpl
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.ui.splash.domain.AppInteractor
import ru.wb.perevozka.ui.splash.domain.AppInteractorImpl
import ru.wb.perevozka.ui.splash.domain.AppSharedRepository
import ru.wb.perevozka.ui.unloadingboxes.domain.UnloadingBoxesInteractor
import ru.wb.perevozka.ui.unloadingboxes.domain.UnloadingBoxesInteractorImpl
import ru.wb.perevozka.ui.unloadingcongratulation.domain.CongratulationInteractorImpl
import ru.wb.perevozka.ui.unloadingforcedtermination.domain.ForcedTerminationInteractor
import ru.wb.perevozka.ui.unloadingforcedtermination.domain.ForcedTerminationInteractorImpl
import ru.wb.perevozka.ui.unloadinghandle.domain.UnloadingHandleInteractor
import ru.wb.perevozka.ui.unloadinghandle.domain.UnloadingHandleInteractorImpl
import ru.wb.perevozka.ui.unloadingreturnboxes.domain.UnloadingReturnInteractor
import ru.wb.perevozka.ui.unloadingreturnboxes.domain.UnloadingReturnInteractorImpl
import ru.wb.perevozka.ui.unloadingscan.domain.UnloadingInteractor
import ru.wb.perevozka.ui.unloadingscan.domain.UnloadingInteractorImpl
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager
import ru.wb.perevozka.ui.unloadingcongratulation.domain.CongratulationInteractor as CongratulationInteractor1

val interactorModule = module {

    fun provideNumberPhoneInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        repository: AuthRemoteRepository,
    ): NumberPhoneInteractor {
        return NumberPhoneInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            repository
        )
    }

    fun provideUserFormInteractorImpl(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
    ): CourierDataInteractor {
        return CourierDataInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository
        )
    }

    fun provideCouriersCompleteRegistrationInteractorImpl(
        rxSchedulerFactory: RxSchedulerFactory,
        refreshTokenRepository: RefreshTokenRepository,
        tokenManager: TokenManager
    ): CourierExpectsInteractor {
        return CourierExpectsInteractorImpl(
            rxSchedulerFactory,
            refreshTokenRepository,
            tokenManager
        )
    }

    fun provideCheckSmsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        repository: AuthRemoteRepository,
    ): CheckSmsInteractor {
        return CheckSmsInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            repository,
        )
    }

    fun provideNavigationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        authRemoteRepository: AuthRemoteRepository,
        appLocalRepository: AppLocalRepository,
        appSharedRepository: AppSharedRepository,
        screenManager: ScreenManager,
    ): AppInteractor {
        return AppInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            authRemoteRepository,
            appLocalRepository,
            appSharedRepository,
            screenManager
        )
    }

    fun provideFlightsLoaderInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        authRemoteRepository: AuthRemoteRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): FlightsLoaderInteractor {
        return FlightsLoaderInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            authRemoteRepository,
            timeManager,
            screenManager
        )
    }

    fun provideFlightsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
    ): FlightsInteractor {
        return FlightsInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository
        )
    }

    fun provideFlightDeliveriesDetailsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): FlightDeliveriesDetailsInteractor {
        return FlightDeliveriesDetailsInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideFlightPickPointInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        screenManager: ScreenManager,
    ): FlightPickPointInteractor {
        return FlightPickPointInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            screenManager
        )
    }

    fun provideFlightDeliveriesInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        screenManager: ScreenManager,
    ): FlightDeliveriesInteractor {
        return FlightDeliveriesInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            screenManager
        )
    }

    fun provideScannerInteractor(
        rxSchedulerFactory: RxSchedulerFactory, scannerRepository: ScannerRepository,
    ): ScannerInteractor {
        return ScannerInteractorImpl(rxSchedulerFactory, scannerRepository)
    }

    fun provideReceptionInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): DcLoadingInteractor {
        return DcLoadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager
        )
    }

    fun provideCourierScannerLoadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
        courierLocalRepository: CourierLocalRepository
    ): CourierLoadingInteractor {
        return CourierLoadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager,
            courierLocalRepository
        )
    }

    fun provideCourierWarehouseInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appSharedRepository: AppSharedRepository,
    ): CourierWarehouseInteractor {
        return CourierWarehouseInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            appSharedRepository
        )
    }

    fun provideCourierOrderDetailsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
    ): CourierOrderDetailsInteractor {
        return CourierOrderDetailsInteractorImpl(rxSchedulerFactory, appRemoteRepository)
    }

    fun provideCourierOrderTimerInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
    ): CourierOrderTimerInteractor {
        return CourierOrderTimerInteractorImpl(rxSchedulerFactory, appRemoteRepository)
    }

    fun provideCourierOrderInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository
    ): CourierOrderInteractor {
        return CourierOrderInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            courierLocalRepository
        )
    }

    fun provideCourierCarNumberInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository
    ): CourierCarNumberInteractor {
        return CourierCarNumberInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
        )
    }

    fun provideUnloadingBoxesInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): UnloadingBoxesInteractor {
        return UnloadingBoxesInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideUnloadingHandleInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): UnloadingHandleInteractor {
        return UnloadingHandleInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideUnloadingReturnInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        timeManager: TimeManager,
    ): UnloadingReturnInteractor {
        return UnloadingReturnInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            timeManager
        )
    }

    fun provideUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): UnloadingInteractor {
        return UnloadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager
        )
    }

    fun provideForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): ForcedTerminationInteractor {
        return ForcedTerminationInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            timeManager,
            screenManager
        )
    }

    fun provideCongratulationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): CongratulationInteractor1 {
        return CongratulationInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    fun provideDcUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): DcUnloadingInteractor {
        return DcUnloadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager
        )
    }

    fun provideDcForcedTerminationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
    ): DcForcedTerminationInteractor {
        return DcForcedTerminationInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            timeManager,
            screenManager
        )
    }

    fun provideDcUnloadingCongratulationInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appLocalRepository: AppLocalRepository,
    ): DcUnloadingCongratulationInteractor {
        return DcUnloadingCongratulationInteractorImpl(rxSchedulerFactory, appLocalRepository)
    }

    single { provideNumberPhoneInteractor(get(), get(), get()) }
    single { provideUserFormInteractorImpl(get(), get(), get()) }
    single { provideCouriersCompleteRegistrationInteractorImpl(get(), get(), get()) }
    single { provideCheckSmsInteractor(get(), get(), get()) }
    single { provideNavigationInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideFlightsLoaderInteractor(get(), get(), get(), get(), get(), get(), get()) }
    single { provideFlightsInteractor(get(), get(), get()) }
    single { provideFlightDeliveriesDetailsInteractor(get(), get()) }
    single { provideFlightPickPointInteractor(get(), get(), get(), get(), get()) }
    single { provideFlightDeliveriesInteractor(get(), get(), get(), get(), get()) }
    factory { provideScannerInteractor(get(), get()) }
    single { provideReceptionInteractor(get(), get(), get(), get(), get(), get(), get()) }

    single { provideCourierWarehouseInteractor(get(), get(), get()) }
    single { provideCourierOrderInteractor(get(), get(), get(), get()) }
    single { provideCourierOrderDetailsInteractor(get(), get()) }
    single { provideCourierOrderTimerInteractor(get(), get()) }
    single { provideCourierCarNumberInteractor(get(), get()) }
    single {
        provideCourierScannerLoadingInteractor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory { provideUnloadingInteractor(get(), get(), get(), get(), get(), get(), get()) }

    single { provideUnloadingBoxesInteractor(get(), get()) }
    single { provideUnloadingHandleInteractor(get(), get()) }
    single { provideUnloadingReturnInteractor(get(), get(), get(), get()) }

    single { provideForcedTerminationInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideCongratulationInteractor(get(), get()) }
    single { provideDcUnloadingInteractor(get(), get(), get(), get(), get(), get(), get()) }
    single { provideDcForcedTerminationInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideDcUnloadingCongratulationInteractor(get(), get()) }

}