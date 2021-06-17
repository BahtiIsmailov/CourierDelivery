package com.wb.logistics.di.module

import com.wb.logistics.db.AppDatabase
import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.AppLocalRepositoryImpl
import com.wb.logistics.db.dao.*
import com.wb.logistics.network.api.app.AppApi
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.AppRemoteRepositoryImpl
import com.wb.logistics.network.api.auth.AuthApi
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.AuthRemoteRepositoryImpl
import com.wb.logistics.network.headers.RefreshTokenApi
import com.wb.logistics.network.headers.RefreshTokenRepository
import com.wb.logistics.network.headers.RefreshTokenRepositoryImpl
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepositoryImpl
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.network.token.UserManager
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import com.wb.logistics.ui.scanner.domain.ScannerRepositoryImpl
import org.koin.dsl.module

val deliveryRepositoryModule = module {

    fun provideAuthRemoteRepository(
        api: AuthApi,
        tokenManager: TokenManager,
        userManager: UserManager,
    ): AuthRemoteRepository {
        return AuthRemoteRepositoryImpl(api, tokenManager, userManager)
    }

    fun provideAppRemoteRepository(api: AppApi, tokenManager: TokenManager): AppRemoteRepository {
        return AppRemoteRepositoryImpl(api, tokenManager)
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
        attachedBoxDao: AttachedBoxDao,
        unloadingBox: UnloadingBoxDao,
        returnBoxDao: ReturnBoxDao,
        dcUnloadingBox: DcUnloadingBoxDao,
        flightMatchingDao: FlightBoxDao,
        warehouseMatchingBoxDao: WarehouseMatchingBoxDao,
    ): AppLocalRepository {
        return AppLocalRepositoryImpl(appDatabase,
            flightDao,
            attachedBoxDao,
            unloadingBox,
            returnBoxDao,
            dcUnloadingBox,
            flightMatchingDao,
            warehouseMatchingBoxDao)
    }

    fun provideScannerRepository(): ScannerRepository {
        return ScannerRepositoryImpl()
    }

    fun provideNetworkMonitorRepository(): NetworkMonitorRepository {
        return NetworkMonitorRepositoryImpl()
    }

    single { provideAuthRemoteRepository(get(), get(), get()) }
    single { provideAppRemoteRepository(get(), get()) }
    single { provideRefreshTokenRepository(get(), get()) }
    single { provideLocalRepository(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { provideScannerRepository() }
    single { provideNetworkMonitorRepository() }

}