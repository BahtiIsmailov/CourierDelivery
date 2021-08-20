package ru.wb.perevozka.di.module

import ru.wb.perevozka.db.AppDatabase
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.AppLocalRepositoryImpl
import ru.wb.perevozka.db.dao.*
import ru.wb.perevozka.network.api.app.AppApi
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.AppRemoteRepositoryImpl
import ru.wb.perevozka.network.api.auth.AuthApi
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.api.auth.AuthRemoteRepositoryImpl
import ru.wb.perevozka.network.headers.RefreshTokenApi
import ru.wb.perevozka.network.headers.RefreshTokenRepository
import ru.wb.perevozka.network.headers.RefreshTokenRepositoryImpl
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepositoryImpl
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.network.token.UserManager
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.ui.scanner.domain.ScannerRepositoryImpl
import ru.wb.perevozka.utils.managers.TimeManager
import org.koin.dsl.module
import ru.wb.perevozka.ui.splash.domain.AppSharedRepository
import ru.wb.perevozka.ui.splash.domain.AppSharedRepositoryImpl

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
    ): AppRemoteRepository {
        return AppRemoteRepositoryImpl(api, tokenManager, timeManager)
    }

    fun provideRefreshTokenRepository(
        api: RefreshTokenApi,
        tokenManager: TokenManager,
    ): RefreshTokenRepository {
        return RefreshTokenRepositoryImpl(api, tokenManager)
    }

    fun provideLocalRepository(
        appDatabase: AppDatabase,
        flightDao: FlightDao,
        flightMatchingDao: FlightBoxDao,
        warehouseMatchingBoxDao: WarehouseMatchingBoxDao,
        pvzMatchingBoxDao: PvzMatchingBoxDao,
        deliveryErrorBoxDao: DeliveryErrorBoxDao,
    ): AppLocalRepository {
        return AppLocalRepositoryImpl(appDatabase,
            flightDao,
            flightMatchingDao,
            warehouseMatchingBoxDao,
            pvzMatchingBoxDao,
            deliveryErrorBoxDao)
    }

    fun provideScannerRepository(): ScannerRepository {
        return ScannerRepositoryImpl()
    }

    fun provideNetworkMonitorRepository(): NetworkMonitorRepository {
        return NetworkMonitorRepositoryImpl()
    }

    fun provideAppSharedRepository(): AppSharedRepository {
        return AppSharedRepositoryImpl()
    }

    single { provideAuthRemoteRepository(get(), get(), get()) }
    single { provideAppRemoteRepository(get(), get(), get()) }
    single { provideRefreshTokenRepository(get(), get()) }
    single { provideLocalRepository(get(), get(), get(), get(), get(), get()) }
    single { provideScannerRepository() }
    single { provideNetworkMonitorRepository() }
    single { provideAppSharedRepository() }

}