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

val interactorModule = module {

    fun provideNumberPhoneInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        repository: AuthRemoteRepository,
    ): NumberPhoneInteractor {
        return NumberPhoneInteractorImpl(
            networkMonitorRepository,
            repository
        )
    }

    fun provideUserFormInteractorImpl(
        networkMonitorRepository: NetworkMonitorRepository,
        appRemoteRepository: AppRemoteRepository,
        userManager: UserManager
    ): CourierDataInteractor {
        return CourierDataInteractorImpl(
            networkMonitorRepository,
            appRemoteRepository,
            userManager
        )
    }

    fun provideCourierBillingAccountDataInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
    ): CourierBillingAccountDataInteractor {
        return CourierBillingAccountDataInteractorImpl(
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository
        )
    }

    fun provideCourierBillingAccountSelectorInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        userManager: UserManager,
        tokenManager: TokenManager
    ): CourierBillingAccountSelectorInteractor {
        return CourierBillingAccountSelectorInteractorImpl(
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            userManager,
            tokenManager
        )
    }

    fun provideCouriersCompleteRegistrationInteractorImpl(
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
        networkMonitorRepository: NetworkMonitorRepository,
        repository: AuthRemoteRepository,
    ): CheckSmsInteractor {
        return CheckSmsInteractorImpl(
            networkMonitorRepository,
            repository,
        )
    }

    fun provideNavigationInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        authRemoteRepository: AuthRemoteRepository,
        appNavRepository: AppNavRepository
    ): AppInteractor {
        return AppInteractorImpl(
            networkMonitorRepository,
            authRemoteRepository,
            appNavRepository
        )
    }

    fun provideScannerInteractor(
        scannerRepository: ScannerRepository,
        settingsManager: SettingsManager
    ): ScannerInteractor {
        return ScannerInteractorImpl(scannerRepository, settingsManager)
    }

    fun provideCourierWarehousesInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppTasksRepository,
        courierLocalRepository: CourierLocalRepository,
        courierMapRepository: CourierMapRepository,
        tokenManager: TokenManager
    ): CourierWarehousesInteractor {
        return CourierWarehousesInteractorImpl(
            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            courierLocalRepository,
            courierMapRepository,
            tokenManager
        )
    }

    fun provideCourierOrdersInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appTasksRepository: AppTasksRepository,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        courierMapRepository: CourierMapRepository,
        userManager: UserManager,
        tokenManager: TokenManager,
        timeManager: TimeManager,
        sharedWorker: SharedWorker
    ): CourierOrdersInteractor {
        return CourierOrdersInteractorImpl(

            networkMonitorRepository,
            deviceManager,
            appTasksRepository,
            appRemoteRepository,
            courierLocalRepository,
            courierMapRepository,
            userManager,
            tokenManager,
            timeManager,
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
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
        timeManager: TimeManager,
    ): CourierOrderTimerInteractor {
        return CourierOrderTimerInteractorImpl(
            appRemoteRepository,
            courierLocalRepository,
            taskTimerRepository,
            timeManager
        )
    }

    fun provideCourierScannerLoadingInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        sharedWorker: SharedWorker,
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
    ): CourierLoadingInteractor {
        return CourierLoadingInteractorImpl(

            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            scannerRepository,
            timeManager,
            sharedWorker = sharedWorker,
            courierLocalRepository,
            taskTimerRepository
        )
    }

    fun provideCourierUnloadingInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        sharedWorker: SharedWorker,
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        courierLocalRepository: CourierLocalRepository,
    ): CourierUnloadingInteractor {
        return CourierUnloadingInteractorImpl(

            networkMonitorRepository,
            deviceManager,
            appRemoteRepository,
            sharedWorker,
            scannerRepository,
            timeManager,
            courierLocalRepository
        )
    }

    fun provideCourierIntransitInteractor(
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository,
        courierLocalRepository: CourierLocalRepository,
        intransitTimeRepository: IntransitTimeRepository,
        timeManager: TimeManager,
        courierMapRepository: CourierMapRepository
    ): CourierIntransitInteractor {
        return CourierIntransitInteractorImpl(
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
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        courierLocalRepository: CourierLocalRepository,
        scannerRepository: ScannerRepository
    ): CourierIntransitOfficeScannerInteractor {
        return CourierIntransitOfficeScannerInteractorImpl(
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
        networkMonitorRepository: NetworkMonitorRepository,
        deviceManager: DeviceManager,
        appRemoteRepository: AppRemoteRepository
    ): CourierBillingInteractor {
        return CourierBillingInteractorImpl(

            networkMonitorRepository,
            deviceManager,
            appRemoteRepository
        )
    }

    fun provideCourierCompleteDeliveryInteractor(
        courierLocalRepository: CourierLocalRepository
    ): CourierCompleteDeliveryInteractor {
        return CourierCompleteDeliveryInteractorImpl(

            courierLocalRepository,
        )
    }

    fun provideCourierVersionControlInteractor(
        courierLocalRepository: CourierLocalRepository
    ): CourierVersionControlInteractor {
        return CourierVersionControlInteractorImpl(
            courierLocalRepository,
        )
    }

    fun provideCourierStartDeliveryInteractor(
        courierLocalRepository: CourierLocalRepository
    ): CourierStartDeliveryInteractor {
        return CourierStartDeliveryInteractorImpl(
            courierLocalRepository,
        )
    }


    fun provideCourierBillingCompleteInteractor(
        courierLocalRepository: CourierLocalRepository
    ): CourierBillingCompleteInteractor {
        return CourierBillingCompleteInteractorImpl(

            courierLocalRepository,
        )
    }

    fun provideSettingsInteractor(
        networkMonitorRepository: NetworkMonitorRepository
    ): SettingsInteractor {
        return SettingsInteractorImpl(networkMonitorRepository)
    }

    single { provideNumberPhoneInteractor(get(), get()) }
    single { provideUserFormInteractorImpl(get(), get(), get()) }
    single { provideCouriersCompleteRegistrationInteractorImpl(get(), get(), get(), get()) }
    single { provideCheckSmsInteractor(get(), get()) }
    single { provideNavigationInteractor(get(), get(), get()) }
    single { provideScannerInteractor(get(), get()) }

    single { provideCourierBillingAccountDataInteractor(get(), get(), get()) }
    single {
        provideCourierBillingAccountSelectorInteractor(
            get(),
            get(),
            get(),
            get(),
            get(),

            )
    }

    factory { provideCourierWarehousesInteractor(get(), get(), get(), get(), get(), get()) }
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

            )
    }
    single { provideCourierCarNumberInteractor(get()) }
    single { provideCourierOrderTimerInteractor(get(), get(), get(), get()) }
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

            )
    }
    factory { provideCourierIntransitOfficeScannerInteractor(get(), get(), get(), get()) }
    factory { provideCourierCompleteDeliveryInteractor(get()) }
    factory { provideCourierVersionControlInteractor(get()) }
    factory { provideCourierStartDeliveryInteractor(get()) }
    factory { provideCourierMapInteractor(get(), get()) }
    factory { provideCourierBillingInteractor(get(), get(), get()) }
    factory { provideCourierBillingCompleteInteractor(get()) }

    single { provideSettingsInteractor(get()) }

}