package com.wb.logistics.di.module

import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.auth.domain.*
import org.koin.dsl.module

val interactorModule = module {

    fun provideTemporaryPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository
    ): TemporaryPasswordInteractor {
        return TemporaryPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideInputPasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository
    ): InputPasswordInteractor {
        return InputPasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    fun provideCreatePasswordInteractor(
        rxSchedulerFactory: RxSchedulerFactory,
        repository: AuthRepository
    ): CreatePasswordInteractor {
        return CreatePasswordInteractorImpl(rxSchedulerFactory, repository)
    }

    single { provideTemporaryPasswordInteractor(get(), get()) }
    single { provideInputPasswordInteractor(get(), get()) }
    single { provideCreatePasswordInteractor(get(), get()) }

}