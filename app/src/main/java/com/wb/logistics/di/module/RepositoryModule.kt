package com.wb.logistics.di.module

import com.wb.logistics.network.api.app.AppApi
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.api.app.AppRepositoryImpl
import com.wb.logistics.network.api.auth.AuthApi
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.api.auth.AuthRepositoryImpl
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
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(api, rxSchedulerFactory, tokenManager)
    }

    fun provideAppRepository(
        api: AppApi,
        rxSchedulerFactory: RxSchedulerFactory
    ): AppRepository {
        return AppRepositoryImpl(api, rxSchedulerFactory)
    }

    fun provideReceptionRepository(api: ReceptionApi, dao: ReceptionDao): ReceptionRepository {
        return ReceptionRepository(api, dao)
    }

    single { provideAuthRepository(get(), get(), get()) }
    single { provideAppRepository(get(), get()) }
    single { provideReceptionRepository(get(), get()) }

}