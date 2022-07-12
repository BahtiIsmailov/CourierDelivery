package ru.wb.go.di.module

import org.koin.dsl.module
import ru.wb.go.db.*
import ru.wb.go.db.dao.CourierBoxDao
import ru.wb.go.db.dao.CourierOrderDao
import ru.wb.go.db.dao.CourierWarehouseDao
import ru.wb.go.network.api.app.*
import ru.wb.go.network.api.auth.AuthApi
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.api.auth.AuthRemoteRepositoryImpl
import ru.wb.go.network.api.refreshtoken.RefreshTokenApi
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepositoryImpl
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkMonitorRepositoryImpl
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.app.domain.AppNavRepository
import ru.wb.go.ui.app.domain.AppNavRepositoryImpl
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.ui.couriermap.domain.CourierMapRepositoryImpl
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerRepositoryImpl
import ru.wb.go.utils.managers.SettingsManager
import ru.wb.go.utils.time.TimeFormatter

val deliveryRepositoryModule = module {

    fun provideAuthRemoteRepository(
        api: AuthApi,
        tokenManager: TokenManager,
        userManager: UserManager,
        settingsManager: SettingsManager
    ): AuthRemoteRepository {
        return AuthRemoteRepositoryImpl(api, tokenManager, userManager, settingsManager)
    }

    fun provideAppRemoteRepository(
        authenticator: AutentificatorIntercept,
        api: AppApi,
        tokenManager: TokenManager,
    ): AppRemoteRepository {
        return AppRemoteRepositoryImpl(authenticator, api, tokenManager)
    }

    fun provideAppTasksRepository(
        authenticator: AutentificatorIntercept,
        apiTasks: AppTasksApi,
        tokenManager: TokenManager
    ): AppTasksRepository {
        return AppTasksRepositoryImpl(authenticator, apiTasks, tokenManager)
    }

    fun provideRefreshTokenRepository(
        api: RefreshTokenApi,
        tokenManager: TokenManager
    ): RefreshTokenRepository {
        return RefreshTokenRepositoryImpl(api, tokenManager)
    }

    fun provideCourierLocalRepository(
        courierWarehouseDao: CourierWarehouseDao,
        courierOrderDao: CourierOrderDao,
        courierBoxDao: CourierBoxDao
    ): CourierLocalRepository {
        return CourierLocalRepositoryImpl(
            courierWarehouseDao,
            courierOrderDao,
            courierBoxDao,
        )
    }

    fun provideTaskTimerRepository(): TaskTimerRepository {
        return TaskTimerRepositoryImpl()
    }

    fun provideIntransitTimeRepository(): IntransitTimeRepository {
        return IntransitTimeRepositoryImpl()
    }

    fun provideScannerRepository(timeFormatter: TimeFormatter): ScannerRepository {
        return ScannerRepositoryImpl(timeFormatter)
    }

    fun provideCourierMapRepository(): CourierMapRepository {
        return CourierMapRepositoryImpl()
    }

    fun provideNetworkMonitorRepository(): NetworkMonitorRepository {
        return NetworkMonitorRepositoryImpl()
    }

    fun provideAutentificatorIntercept():AutentificatorIntercept{
        return AutentificatorIntercept()
    }

    fun provideAppNavRepository(): AppNavRepository {
        return AppNavRepositoryImpl()
    }

    single { provideAuthRemoteRepository(get(), get(), get(), get()) }

    single { provideAppRemoteRepository(get(), get(), get()) }
    factory { provideAppTasksRepository(get(), get(), get()) }

    single { provideAutentificatorIntercept() }

    single { provideRefreshTokenRepository(get(), get()) }
    single { provideCourierLocalRepository(get(), get(), get()) }
    single { provideCourierMapRepository() }
    single { provideTaskTimerRepository() }
    single { provideIntransitTimeRepository() }
    single { provideScannerRepository(get()) }
    single { provideNetworkMonitorRepository() }
    single { provideAppNavRepository() }

}