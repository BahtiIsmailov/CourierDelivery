package com.wb.logistics.network.rx

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RxSchedulerFactoryImpl : RxSchedulerFactory {

    override fun computation(): Scheduler {
        return Schedulers.computation()
    }

    override fun <T> applySingleSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer { upstream: Single<T> ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun <T> applyObservableSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer { observer: Observable<T> ->
            observer
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
        }
    }

    override fun applyCompletableSchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream: Completable ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
        }
    }

}