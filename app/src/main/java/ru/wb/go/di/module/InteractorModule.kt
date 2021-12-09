package ru.wb.go.di.module

import org.koin.dsl.module
import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.headers.RefreshTokenRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.auth.domain.CheckSmsInteractor
import ru.wb.go.ui.auth.domain.CheckSmsInteractorImpl
import ru.wb.go.ui.auth.domain.NumberPhoneInteractor
import ru.wb.go.ui.auth.domain.NumberPhoneInteractorImpl
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractor
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractorImpl
import ru.wb.go.ui.courierbillingaccountdata.domain.CourierBillingAccountDataInteractor
import ru.wb.go.ui.courierbillingaccountdata.domain.CourierBillingAccountDataInteractorImpl
import ru.wb.go.ui.courierbillingaccountselector.domain.CourierBillingAccountSelectorInteractor
import ru.wb.go.ui.courierbillingaccountselector.domain.CourierBillingAccountSelectorInteractorImpl
import ru.wb.go.ui.couriercarnumber.domain.CourierCarNumberInteractor
import ru.wb.go.ui.couriercarnumber.domain.CourierCarNumberInteractorImpl
import ru.wb.go.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor
import ru.wb.go.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractorImpl
import ru.wb.go.ui.courierdata.domain.CourierDataInteractor
import ru.wb.go.ui.courierdata.domain.CourierDataInteractorImpl
import ru.wb.go.ui.courierexpects.domain.CourierExpectsInteractor
import ru.wb.go.ui.courierexpects.domain.CourierExpectsInteractorImpl
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractorImpl
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractor
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractorImpl
import ru.wb.go.ui.couriermap.domain.CourierMapInteractor
import ru.wb.go.ui.couriermap.domain.CourierMapInteractorImpl
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.ui.courierorderconfirm.domain.CourierOrderConfirmInteractor
import ru.wb.go.ui.courierorderconfirm.domain.CourierOrderConfirmInteractorImpl
import ru.wb.go.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.go.ui.courierorderdetails.domain.CourierOrderDetailsInteractorImpl
import ru.wb.go.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.go.ui.courierorders.domain.CourierOrderInteractorImpl
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractorImpl
import ru.wb.go.ui.courierstartdelivery.domain.CourierStartDeliveryInteractor
import ru.wb.go.ui.courierstartdelivery.domain.CourierStartDeliveryInteractorImpl
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractor
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractorImpl
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehouseInteractorImpl
import ru.wb.go.ui.dcloading.domain.DcLoadingInteractor
import ru.wb.go.ui.dcloading.domain.DcLoadingInteractorImpl
import ru.wb.go.ui.dcunloading.domain.DcUnloadingInteractor
import ru.wb.go.ui.dcunloading.domain.DcUnloadingInteractorImpl
import ru.wb.go.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import ru.wb.go.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractorImpl
import ru.wb.go.ui.dcunloadingforcedtermination.domain.DcForcedTerminationInteractor
import ru.wb.go.ui.dcunloadingforcedtermination.domain.DcForcedTerminationInteractorImpl
import ru.wb.go.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import ru.wb.go.ui.flightdeliveries.domain.FlightDeliveriesInteractorImpl
import ru.wb.go.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractor
import ru.wb.go.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractorImpl
import ru.wb.go.ui.flightpickpoint.domain.FlightPickPointInteractor
import ru.wb.go.ui.flightpickpoint.domain.FlightPickPointInteractorImpl
import ru.wb.go.ui.flights.domain.FlightsInteractor
import ru.wb.go.ui.flights.domain.FlightsInteractorImpl
import ru.wb.go.ui.flightsloader.domain.FlightsLoaderInteractor
import ru.wb.go.ui.flightsloader.domain.FlightsLoaderInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.splash.domain.AppInteractor
import ru.wb.go.ui.splash.domain.AppInteractorImpl
import ru.wb.go.ui.splash.domain.AppSharedRepository
import ru.wb.go.ui.unloadingboxes.domain.UnloadingBoxesInteractor
import ru.wb.go.ui.unloadingboxes.domain.UnloadingBoxesInteractorImpl
import ru.wb.go.ui.unloadingcongratulation.domain.CongratulationInteractorImpl
import ru.wb.go.ui.unloadingforcedtermination.domain.ForcedTerminationInteractor
import ru.wb.go.ui.unloadingforcedtermination.domain.ForcedTerminationInteractorImpl
import ru.wb.go.ui.unloadinghandle.domain.UnloadingHandleInteractor
import ru.wb.go.ui.unloadinghandle.domain.UnloadingHandleInteractorImpl
import ru.wb.go.ui.unloadingreturnboxes.domain.UnloadingReturnInteractor
import ru.wb.go.ui.unloadingreturnboxes.domain.UnloadingReturnInteractorImpl
import ru.wb.go.ui.unloadingscan.domain.UnloadingInteractor
import ru.wb.go.ui.unloadingscan.domain.UnloadingInteractorImpl
import ru.wb.go.utils.managers.ScreenManager
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.time.TimeFormatter
import ru.wb.go.ui.unloadingcongratulation.domain.CongratulationInteractor as CongratulationInteractor1

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

    fun provideCourierBillingAccountDataInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
    ): CourierBillingAccountDataInteractor {
        return CourierBillingAccountDataInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            courierLocalRepository
        )
    }

    fun provideCourierBillingAccountSelectorInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
    ): CourierBillingAccountSelectorInteractor {
        return CourierBillingAccountSelectorInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            courierLocalRepository
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

    fun provideCourierWarehouseInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appSharedRepository: AppSharedRepository,
        courierLocalRepository: CourierLocalRepository,
        courierMapRepository: CourierMapRepository
    ): CourierWarehouseInteractor {
        return CourierWarehouseInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            appSharedRepository,
            courierLocalRepository,
            courierMapRepository
        )
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

    fun provideCourierOrderDetailsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        courierLocalRepository: CourierLocalRepository,
        userManager: UserManager,
        courierMapRepository: CourierMapRepository
    ): CourierOrderDetailsInteractor {
        return CourierOrderDetailsInteractorImpl(
            rxSchedulerFactory,
            courierLocalRepository,
            userManager,
            courierMapRepository
        )
    }

    fun provideCourierCarNumberInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        userManager: UserManager
    ): CourierCarNumberInteractor {
        return CourierCarNumberInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            userManager
        )
    }

    fun provideCourierOrderTimerInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
        timeFormatter: TimeFormatter,
        userManager: UserManager
    ): CourierOrderTimerInteractor {
        return CourierOrderTimerInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            courierLocalRepository,
            taskTimerRepository,
            timeFormatter,
            userManager
        )
    }

    fun provideCourierOrderConfirmInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        userManager: UserManager
    ): CourierOrderConfirmInteractor {
        return CourierOrderConfirmInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            courierLocalRepository,
            userManager
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
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
        userManager: UserManager
    ): CourierLoadingInteractor {
        return CourierLoadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            appLocalRepository,
            scannerRepository,
            timeManager,
            screenManager,
            courierLocalRepository,
            taskTimerRepository,
            userManager
        )
    }

    fun provideCourierUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        screenManager: ScreenManager,
        courierLocalRepository: CourierLocalRepository,
    ): CourierUnloadingInteractor {
        return CourierUnloadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            scannerRepository,
            timeManager,
            screenManager,
            courierLocalRepository
        )
    }

    fun provideCourierIntransitInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        scannerRepository: ScannerRepository,
        intransitTimeRepository: IntransitTimeRepository,
        timeManager: TimeManager,
        timeFormatter: TimeFormatter,
        courierMapRepository: CourierMapRepository
    ): CourierIntransitInteractor {
        return CourierIntransitInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            courierLocalRepository,
            scannerRepository,
            intransitTimeRepository,
            timeManager,
            timeFormatter,
            courierMapRepository
        )
    }

    fun provideCourierMapInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        courierMapRepository: CourierMapRepository,
    ): CourierMapInteractor {
        return CourierMapInteractorImpl(rxSchedulerFactory, courierMapRepository)
    }

    fun provideCourierBillingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository
    ): CourierBillingInteractor {
        return CourierBillingInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            courierLocalRepository
        )
    }

    fun provideCourierCompleteDeliveryInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        courierLocalRepository: CourierLocalRepository
    ): CourierCompleteDeliveryInteractor {
        return CourierCompleteDeliveryInteractorImpl(
            rxSchedulerFactory,
            courierLocalRepository,
        )
    }

    fun provideCourierStartDeliveryInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        courierLocalRepository: CourierLocalRepository
    ): CourierStartDeliveryInteractor {
        return CourierStartDeliveryInteractorImpl(
            rxSchedulerFactory,
            courierLocalRepository,
        )
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

    single { provideCourierBillingAccountDataInteractor(get(), get(), get(), get()) }
    single { provideCourierBillingAccountSelectorInteractor(get(), get(), get(), get()) }

    factory { provideUnloadingInteractor(get(), get(), get(), get(), get(), get(), get()) }

    single { provideUnloadingBoxesInteractor(get(), get()) }
    single { provideUnloadingHandleInteractor(get(), get()) }
    single { provideUnloadingReturnInteractor(get(), get(), get(), get()) }

    single { provideForcedTerminationInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideCongratulationInteractor(get(), get()) }
    single { provideDcUnloadingInteractor(get(), get(), get(), get(), get(), get(), get()) }
    single { provideDcForcedTerminationInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideDcUnloadingCongratulationInteractor(get(), get()) }

    // TODO: 15.09.2021 вынести в отдельный модуль
    single { provideCourierWarehouseInteractor(get(), get(), get(), get(), get()) }
    single { provideCourierOrderInteractor(get(), get(), get(), get()) }
    single { provideCourierOrderDetailsInteractor(get(), get(), get(), get()) }
    single { provideCourierCarNumberInteractor(get(), get(), get()) }
    single { provideCourierOrderTimerInteractor(get(), get(), get(), get(), get(), get()) }
    single { provideCourierOrderConfirmInteractor(get(), get(), get(), get()) }
    factory {
        provideCourierScannerLoadingInteractor(
            get(),
            get(),
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
    factory {
        provideCourierUnloadingInteractor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory {
        provideCourierIntransitInteractor(
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
    factory { provideCourierCompleteDeliveryInteractor(get(), get()) }
    factory { provideCourierStartDeliveryInteractor(get(), get()) }
    factory { provideCourierMapInteractor(get(), get()) }
    factory { provideCourierBillingInteractor(get(), get(), get()) }

}