package ru.wb.go.di.module

import org.koin.dsl.module
import ru.wb.go.db.*
import ru.wb.go.db.dao.CourierBoxDao
import ru.wb.go.db.dao.CourierOrderDao
import ru.wb.go.db.dao.CourierWarehouseDao
import ru.wb.go.network.api.app.AppApi
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.AppRemoteRepositoryImpl
import ru.wb.go.network.api.auth.AuthApi
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.api.auth.AuthRemoteRepositoryImpl
import ru.wb.go.network.headers.RefreshTokenApi
import ru.wb.go.network.headers.RefreshTokenRepository
import ru.wb.go.network.headers.RefreshTokenRepositoryImpl
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkMonitorRepositoryImpl
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.ui.couriermap.domain.CourierMapRepositoryImpl
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerRepositoryImpl
import ru.wb.go.ui.splash.domain.AppSharedRepository
import ru.wb.go.ui.splash.domain.AppSharedRepositoryImpl
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.TimeManager

val deliveryRepositoryModule = module {

    fun provideAuthRemoteRepository(
        api: AuthApi,
        tokenManager: TokenManager,
        userManager: UserManager,
    ): AuthRemoteRepository {
        return AuthRemoteRepositoryImpl(api, tokenManager, userManager)
    }

    fun provideAppRemoteRepository(
        api: AppApi,
        tokenManager: TokenManager,
        timeManager: TimeManager,
        metric: YandexMetricManager
    ): AppRemoteRepository {
        return AppRemoteRepositoryImpl(api, tokenManager, timeManager, metric)
    }

    fun provideRefreshTokenRepository(
        api: RefreshTokenApi,
        tokenManager: TokenManager,
    ): RefreshTokenRepository {
        return RefreshTokenRepositoryImpl(api, tokenManager)
    }

    fun provideCourierLocalRepository(
        courierWarehouseDao: CourierWarehouseDao,
        courierOrderDao: CourierOrderDao,
        courierBoxDao: CourierBoxDao,
    ): CourierLocalRepository {
        return CourierLocalRepositoryImpl(
            courierWarehouseDao,
            courierOrderDao,
            courierBoxDao
        )
    }

    fun provideTaskTimerRepository(): TaskTimerRepository {
        return TaskTimerRepositoryImpl()
    }

    fun provideIntransitTimeRepository(): IntransitTimeRepository {
        return IntransitTimeRepositoryImpl()
    }

    fun provideScannerRepository(): ScannerRepository {
        return ScannerRepositoryImpl()
    }

    fun provideCourierMapRepository(): CourierMapRepository {
        return CourierMapRepositoryImpl()
    }

    fun provideNetworkMonitorRepository(): NetworkMonitorRepository {
        return NetworkMonitorRepositoryImpl()
    }

    fun provideAppSharedRepository(): AppSharedRepository {
        return AppSharedRepositoryImpl()
    }

    single { provideAuthRemoteRepository(get(), get(), get()) }
    single { provideAppRemoteRepository(get(), get(), get(), get()) }
    single { provideRefreshTokenRepository(get(), get()) }
    single { provideCourierLocalRepository(get(), get(), get()) }
    single { provideCourierMapRepository() }
    single { provideTaskTimerRepository() }
    single { provideIntransitTimeRepository() }
    single { provideScannerRepository() }
    single { provideNetworkMonitorRepository() }
    single { provideAppSharedRepository() }

}