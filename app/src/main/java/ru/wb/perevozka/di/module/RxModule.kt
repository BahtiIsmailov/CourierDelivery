package ru.wb.perevozka.di.module

import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.rx.RxSchedulerFactoryImpl
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