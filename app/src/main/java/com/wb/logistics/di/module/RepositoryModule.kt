package com.wb.logistics.di.module

import com.wb.logistics.data.AppRepository
import com.wb.logistics.data.AppRepositoryImpl
import com.wb.logistics.db.BoxDao
import com.wb.logistics.db.FlightDao
import com.wb.logistics.db.LocalRepository
import com.wb.logistics.db.LocalRepositoryImpl
import com.wb.logistics.network.api.app.RemoteRepository
import com.wb.logistics.network.api.auth.AuthApi
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.api.auth.AuthRepositoryImpl
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepositoryImpl
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.ui.reception.data.ReceptionApi
import com.wb.logistics.ui.reception.data.ReceptionDao
import com.wb.logistics.ui.reception.data.ReceptionRepository
import org.koin.dsl.module

val deliveryRepositoryModule = module {

    fun provideAuthRepository(
        api: AuthApi,
        rxSchedulerFactory: RxSchedulerFactory,
        tokenManager: TokenManager,
    ): AuthRepository {
        return AuthRepositoryImpl(api, rxSchedulerFactory, tokenManager)
    }

    fun provideAppRepository(
        remoteRepository: RemoteRepository,
        localRepository: LocalRepository,
    ): AppRepository {
        return AppRepositoryImpl(remoteRepository, localRepository)
    }

    fun provideLocalRepository(
        flightDao: FlightDao,
        boxDao: BoxDao,
    ): LocalRepository {
        return LocalRepositoryImpl(flightDao, boxDao)
    }

    fun provideReceptionRepository(api: ReceptionApi, dao: ReceptionDao): ReceptionRepository {
        return ReceptionRepository(api, dao)
    }

    fun provideNetworkMonitorRepository(): NetworkMonitorRepository {
        return NetworkMonitorRepositoryImpl()
    }

    single { provideAuthRepository(get(), get(), get()) }
    single { provideLocalRepository(get(), get()) }
    single { provideAppRepository(get(), get()) }
    single { provideReceptionRepository(get(), get()) }
    single { provideNetworkMonitorRepository() }

}