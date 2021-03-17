package com.wb.logistics.di.module

import com.wb.logistics.network.api.AuthRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.auth.domain.InputPasswordInteractor
import com.wb.logistics.ui.auth.domain.InputPasswordInteractorImpl
import com.wb.logistics.ui.auth.domain.TemporaryPasswordInteractor
import com.wb.logistics.ui.auth.domain.TemporaryPasswordInteractorImpl
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

    single { provideTemporaryPasswordInteractor(get(), get()) }
    single { provideInputPasswordInteractor(get(), get()) }

}