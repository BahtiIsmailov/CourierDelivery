package ru.wb.go.di.module

import io.reactivex.disposables.CompositeDisposable
import org.koin.dsl.module
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.rx.RxSchedulerFactoryImpl
import ru.wb.go.utils.analytics.YandexMetricManager

val rxModule = module {

    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    fun provideRxSchedulerFactory(metric: YandexMetricManager): RxSchedulerFactory {
        return RxSchedulerFactoryImpl(metric)
    }

    factory { provideCompositeDisposable() }
    single { provideRxSchedulerFactory(get()) }

}