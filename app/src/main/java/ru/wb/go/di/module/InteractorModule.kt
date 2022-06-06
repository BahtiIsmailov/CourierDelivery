package ru.wb.go.di.module

import org.koin.dsl.module
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.AppTasksRepository
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.app.domain.AppInteractor
import ru.wb.go.ui.app.domain.AppInteractorImpl
import ru.wb.go.ui.app.domain.AppNavRepository
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
import ru.wb.go.ui.courierbilllingcomplete.domain.CourierBillingCompleteInteractor
import ru.wb.go.ui.courierbilllingcomplete.domain.CourierBillingCompleteInteractorImpl
import ru.wb.go.ui.couriercarnumber.domain.CourierCarNumberInteractor
import ru.wb.go.ui.couriercarnumber.domain.CourierCarNumberInteractorImpl
import ru.wb.go.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor
import ru.wb.go.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractorImpl
import ru.wb.go.ui.courierdata.domain.CourierDataInteractor
import ru.wb.go.ui.courierdata.domain.CourierDataInteractorImpl
import ru.wb.go.ui.courierdataexpects.domain.CourierDataExpectsInteractor
import ru.wb.go.ui.courierdataexpects.domain.CourierDataExpectsInteractorImpl
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractorImpl
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScannerInteractor
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScannerInteractorImpl
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractor
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractorImpl
import ru.wb.go.ui.couriermap.domain.CourierMapInteractor
import ru.wb.go.ui.couriermap.domain.CourierMapInteractorImpl
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.ui.courierorders.domain.CourierOrdersInteractor
import ru.wb.go.ui.courierorders.domain.CourierOrdersInteractorImpl
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractorImpl
import ru.wb.go.ui.courierstartdelivery.domain.CourierStartDeliveryInteractor
import ru.wb.go.ui.courierstartdelivery.domain.CourierStartDeliveryInteractorImpl
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractor
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractorImpl
import ru.wb.go.ui.courierversioncontrol.domain.CourierVersionControlInteractor
import ru.wb.go.ui.courierversioncontrol.domain.CourierVersionControlInteractorImpl
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehousesInteractor
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehousesInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.settings.domain.SettingsInteractor
import ru.wb.go.ui.settings.domain.SettingsInteractorImpl
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.prefs.SharedWorker
import ru.wb.go.utils.time.TimeFormatter

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
        userManager: UserManager
    ): CourierDataInteractor {
        return CourierDataInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            userManager
        )
    }

    fun provideCourierBillingAccountDataInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
    ): CourierBillingAccountDataInteractor {
        return CourierBillingAccountDataInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository
        )
    }

    fun provideCourierBillingAccountSelectorInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        userManager: UserManager,
        tokenManager: TokenManager
    ): CourierBillingAccountSelectorInteractor {
        return CourierBillingAccountSelectorInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            userManager,
            tokenManager
        )
    }

    fun provideCouriersCompleteRegistrationInteractorImpl(
        rxSchedulerFactory: RxSchedulerFactory,
        refreshTokenRepository: RefreshTokenRepository,
        appRemoteRepository: AppRemoteRepository,
        tokenManager: TokenManager,
        userManager: UserManager
    ): CourierDataExpectsInteractor {
        return CourierDataExpectsInteractorImpl(
            refreshTokenRepository,
            appRemoteRepository,
            tokenManager,
            userManager
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
        appNavRepository: AppNavRepository
    ): AppInteractor {
        return AppInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            authRemoteRepository,
            appNavRepository
        )
    }

    fun provideScannerInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        scannerRepository: ScannerRepository,
        settingsManager: SettingsManager
    ): ScannerInteractor {
        return ScannerInteractorImpl(rxSchedulerFactory, scannerRepository, settingsManager)
    }

    fun provideCourierWarehousesInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppTasksRepository,
        courierLocalRepository: CourierLocalRepository,
        courierMapRepository: CourierMapRepository,
        tokenManager: TokenManager
    ): CourierWarehousesInteractor {
        return CourierWarehousesInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            courierLocalRepository,
            courierMapRepository,
            tokenManager
        )
    }

    fun provideCourierOrdersInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appTasksRepository: AppTasksRepository,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        courierMapRepository: CourierMapRepository,
        userManager: UserManager,
        tokenManager: TokenManager,
        timeManager: TimeManager,
        timeFormatter: TimeFormatter,
        sharedWorker: SharedWorker
    ): CourierOrdersInteractor {
        return CourierOrdersInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appTasksRepository,
            appRemoteRepository,
            courierLocalRepository,
            courierMapRepository,
            userManager,
            tokenManager,
            timeManager,
            timeFormatter,
            sharedWorker
        )
    }

    fun provideCourierCarNumberInteractor(
        userManager: UserManager
    ): CourierCarNumberInteractor {
        return CourierCarNumberInteractorImpl(
            userManager
        )
    }

    fun provideCourierOrderTimerInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
        timeFormatter: TimeFormatter,
        timeManager: TimeManager,
    ): CourierOrderTimerInteractor {
        return CourierOrderTimerInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            courierLocalRepository,
            taskTimerRepository,
            timeFormatter,
            timeManager
        )
    }

    fun provideCourierScannerLoadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
    ): CourierLoadingInteractor {
        return CourierLoadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            scannerRepository,
            timeManager,
            courierLocalRepository,
            taskTimerRepository
        )
    }

    fun provideCourierUnloadingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        courierLocalRepository: CourierLocalRepository,
    ): CourierUnloadingInteractor {
        return CourierUnloadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            scannerRepository,
            timeManager,
            courierLocalRepository
        )
    }

    fun provideCourierIntransitInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        intransitTimeRepository: IntransitTimeRepository,
        timeManager: TimeManager,
        courierMapRepository: CourierMapRepository
    ): CourierIntransitInteractor {
        return CourierIntransitInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            courierLocalRepository,
            intransitTimeRepository,
            timeManager,
            courierMapRepository
        )
    }

    fun provideCourierIntransitOfficeScannerInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        courierLocalRepository: CourierLocalRepository,
        scannerRepository: ScannerRepository
    ): CourierIntransitOfficeScannerInteractor {
        return CourierIntransitOfficeScannerInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            courierLocalRepository,
            scannerRepository
        )
    }

    fun provideCourierMapInteractor(
        courierMapRepository: CourierMapRepository,
        deviceManager: DeviceManager
    ): CourierMapInteractor {
        return CourierMapInteractorImpl(courierMapRepository, deviceManager)
    }

    fun provideCourierBillingInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository
    ): CourierBillingInteractor {
        return CourierBillingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository
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

    fun provideCourierVersionControlInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        courierLocalRepository: CourierLocalRepository
    ): CourierVersionControlInteractor {
        return CourierVersionControlInteractorImpl(
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


    fun provideCourierBillingCompleteInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        courierLocalRepository: CourierLocalRepository
    ): CourierBillingCompleteInteractor {
        return CourierBillingCompleteInteractorImpl(
            rxSchedulerFactory,
            courierLocalRepository,
        )
    }

    fun provideSettingsInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        networkMonitorRepository: NetworkMonitorRepository
    ): SettingsInteractor {
        return SettingsInteractorImpl(rxSchedulerFactory, networkMonitorRepository)
    }

    single { provideNumberPhoneInteractor(get(), get(), get()) }
    single { provideUserFormInteractorImpl(get(), get(), get(), get()) }
    single { provideCouriersCompleteRegistrationInteractorImpl(get(), get(), get(), get(), get()) }
    single { provideCheckSmsInteractor(get(), get(), get()) }
    single { provideNavigationInteractor(get(), get(), get(), get()) }
    single { provideScannerInteractor(get(), get(), get()) }

    single { provideCourierBillingAccountDataInteractor(get(), get(), get(), get()) }
    single {
        provideCourierBillingAccountSelectorInteractor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory { provideCourierWarehousesInteractor(get(), get(), get(), get(), get(), get(), get()) }
    factory {
        provideCourierOrdersInteractor(
            get(),
            get(),
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
    single { provideCourierCarNumberInteractor(get()) }
    single { provideCourierOrderTimerInteractor(get(), get(), get(), get(), get(), get()) }
    factory {
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
    factory { provideCourierIntransitOfficeScannerInteractor(get(), get(), get(), get(), get()) }
    factory { provideCourierCompleteDeliveryInteractor(get(), get()) }
    factory { provideCourierVersionControlInteractor(get(), get()) }
    factory { provideCourierStartDeliveryInteractor(get(), get()) }
    factory { provideCourierMapInteractor(get(), get()) }
    factory { provideCourierBillingInteractor(get(), get(), get(), get()) }
    factory { provideCourierBillingCompleteInteractor(get(), get()) }

    single { provideSettingsInteractor(get(), get()) }

}