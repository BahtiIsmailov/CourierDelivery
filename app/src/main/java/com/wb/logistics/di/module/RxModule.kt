package com.wb.logistics.di.module

import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.rx.RxSchedulerFactoryImpl
import io.reactivex.disposables.CompositeDisposable
import org.koin.dsl.module

val rxModule = module {

    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    fun provideRxSchedulerFactory(): RxSchedulerFactory {
        return RxSchedulerFactoryImpl()
    }

    single { provideCompositeDisposable() }
    single { provideRxSchedulerFactory() }
}