package com.wb.logistics.di.module

import com.wb.logistics.db.LocalRepository
import com.wb.logistics.db.LocalRepositoryImpl
import com.wb.logistics.db.dao.AttachedBoxDao
import com.wb.logistics.db.dao.FlightDao
import com.wb.logistics.db.dao.ReturnBoxDao
import com.wb.logistics.db.dao.UnloadingBoxDao
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.api.app.AppRepositoryImpl
import com.wb.logistics.network.api.app.RemoteAppRepository
import com.wb.logistics.network.api.auth.AuthApi
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.api.auth.AuthRepositoryImpl
import com.wb.logistics.network.headers.RefreshTokenApi
import com.wb.logistics.network.headers.RefreshTokenRepository
import com.wb.logistics.network.headers.RefreshTokenRepositoryImpl
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepositoryImpl
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.ui.reception.data.ReceptionApi
import com.wb.logistics.ui.reception.data.ReceptionDao
import com.wb.logistics.ui.reception.data.ReceptionRepository
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import com.wb.logistics.ui.scanner.domain.ScannerRepositoryImpl
import org.koin.dsl.module

val deliveryRepositoryModule = module {

    fun provideAuthRepository(
        api: AuthApi,
        rxSchedulerFactory: RxSchedulerFactory,
        tokenManager: TokenManager,
    ): AuthRepository {
        return AuthRepositoryImpl(api, rxSchedulerFactory, tokenManager)
    }

    fun provideRefreshTokenRepository(
        api: RefreshTokenApi,
        tokenManager: TokenManager,
    ): RefreshTokenRepository {
        return RefreshTokenRepositoryImpl(api, tokenManager)
    }

    fun provideAppRepository(
        remoteRepository: RemoteAppRepository,
        localRepository: LocalRepository,
        timeManager: TimeManager,
    ): AppRepository {
        return AppRepositoryImpl(remoteRepository, localRepository, timeManager)
    }

    fun provideLocalRepository(
        flightDao: FlightDao,
        attachedBoxDao: AttachedBoxDao,
        unloadingBox: UnloadingBoxDao,
        returnBoxDao: ReturnBoxDao,
    ): LocalRepository {
        return LocalRepositoryImpl(flightDao, attachedBoxDao, unloadingBox, returnBoxDao)
    }

    fun provideReceptionRepository(api: ReceptionApi, dao: ReceptionDao): ReceptionRepository {
        return ReceptionRepository(api, dao)
    }

    fun provideScannerRepository(): ScannerRepository {
        return ScannerRepositoryImpl()
    }

    fun provideNetworkMonitorRepository(): NetworkMonitorRepository {
        return NetworkMonitorRepositoryImpl()
    }

    single { provideAuthRepository(get(), get(), get()) }
    single { provideRefreshTokenRepository(get(), get()) }
    single { provideLocalRepository(get(), get(), get(), get()) }
    single { provideAppRepository(get(), get(), get()) }
    single { provideReceptionRepository(get(), get()) }
    single { provideScannerRepository() }
    single { provideNetworkMonitorRepository() }

}