package com.wb.logistics.di.module

import com.wb.logistics.network.api.AuthApi
import com.wb.logistics.ui.auth.data.AuthRepository
import com.wb.logistics.ui.auth.data.AuthRepositoryImpl
import com.wb.logistics.ui.reception.data.ReceptionApi
import com.wb.logistics.ui.reception.data.ReceptionDao
import com.wb.logistics.ui.reception.data.ReceptionRepository
import org.koin.dsl.module

val deliveryRepositoryModule = module {

    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    fun provideReceptionRepository(api: ReceptionApi, dao: ReceptionDao): ReceptionRepository {
        return ReceptionRepository(api, dao)
    }

    single { provideAuthRepository(get()) }
    single { provideReceptionRepository(get(), get()) }
}