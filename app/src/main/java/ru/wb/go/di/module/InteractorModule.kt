package ru.wb.go.di.module

import org.koin.dsl.module
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
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.splash.domain.AppInteractor
import ru.wb.go.ui.splash.domain.AppInteractorImpl
import ru.wb.go.ui.splash.domain.AppSharedRepository
import ru.wb.go.utils.managers.TimeManager
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
        appSharedRepository: AppSharedRepository
    ): AppInteractor {
        return AppInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            authRemoteRepository,
            appSharedRepository
        )
    }

    fun provideScannerInteractor(
        rxSchedulerFactory: RxSchedulerFactory, scannerRepository: ScannerRepository,
    ): ScannerInteractor {
        return ScannerInteractorImpl(rxSchedulerFactory, scannerRepository)
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
        scannerRepository: ScannerRepository,
        timeManager: TimeManager,
        courierLocalRepository: CourierLocalRepository,
        taskTimerRepository: TaskTimerRepository,
        userManager: UserManager
    ): CourierLoadingInteractor {
        return CourierLoadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            scannerRepository,
            timeManager,
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
        courierLocalRepository: CourierLocalRepository,
    ): CourierUnloadingInteractor {
        return CourierUnloadingInteractorImpl(
            rxSchedulerFactory,
            networkMonitorRepository,
            appRemoteRepository,
            scannerRepository,
            timeManager,
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
        courierLocalRepository: CourierLocalRepository,
        tokenManager: TokenManager
    ): CourierBillingInteractor {
        return CourierBillingInteractorImpl(
            rxSchedulerFactory,
            appRemoteRepository,
            courierLocalRepository,
            tokenManager
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
    single { provideNavigationInteractor(get(), get(), get(), get()) }
    factory { provideScannerInteractor(get(), get()) }

    single { provideCourierBillingAccountDataInteractor(get(), get(), get(), get()) }
    single { provideCourierBillingAccountSelectorInteractor(get(), get(), get(), get()) }

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
    factory { provideCourierBillingInteractor(get(), get(), get(), get()) }

}