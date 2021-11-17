package ru.wb.go.di.module

import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.rx.RxSchedulerFactoryImpl
import io.reactivex.disposables.CompositeDisposable
import org.koin.dsl.module

val rxModule = module {

    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    fun provideRxSchedulerFactory(): RxSchedulerFactory {
        return RxSchedulerFactoryImpl()
    }

    factory { provideCompositeDisposable() }
    single { provideRxSchedulerFactory() }
}