package com.wb.logistics.network.rx

import io.reactivex.CompletableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.SingleTransformer

interface RxSchedulerFactory {
    fun computation(): Scheduler
    fun <T> applySingleSchedulers(): SingleTransformer<T, T>
    fun <T> applyObservableSchedulers(): ObservableTransformer<T, T>
    fun applyCompletableSchedulers(): CompletableTransformer
}