package com.wb.logistics.network.rx

import io.reactivex.*

interface RxSchedulerFactory {
    fun computation(): Scheduler
    fun <T> applySingleSchedulers(): SingleTransformer<T, T>
    fun <T> applyObservableSchedulers(): ObservableTransformer<T, T>
    fun <T> applyFlowableSchedulers(): FlowableTransformer<T, T>
    fun applyCompletableSchedulers(): CompletableTransformer
}