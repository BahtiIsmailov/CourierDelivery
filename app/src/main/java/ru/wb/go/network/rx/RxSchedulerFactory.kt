package ru.wb.go.network.rx

import io.reactivex.*

interface RxSchedulerFactory {
    fun computation(): Scheduler
    fun <T> applySingleSchedulers(): SingleTransformer<T, T>
    fun <T> applyObservableSchedulers(): ObservableTransformer<T, T>
    fun <T> applyFlowableSchedulers(): FlowableTransformer<T, T>
    fun applyCompletableSchedulers(): CompletableTransformer

    fun <T> applySingleMetrics(method: String): SingleTransformer<T, T>
    fun <T> applyObservableMetrics(method: String): ObservableTransformer<T, T>
    fun <T> applyFlowableMetrics(method: String): FlowableTransformer<T, T>
    fun applyCompletableMetrics(method: String): CompletableTransformer
}