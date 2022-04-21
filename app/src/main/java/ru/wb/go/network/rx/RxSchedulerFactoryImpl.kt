package ru.wb.go.network.rx

import android.util.Log
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.wb.go.utils.analytics.YandexMetricManager

class RxSchedulerFactoryImpl(private val metric: YandexMetricManager) : RxSchedulerFactory {

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

    override fun <T> applyFlowableSchedulers(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream: Flowable<T> ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
        }
    }

    override fun <T> applyMaybeSchedulers(): MaybeTransformer<T, T> {
        return MaybeTransformer { upstream: Maybe<T> ->
            upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
        }
    }

    override fun <T> applySingleMetrics(method: String): SingleTransformer<T, T> {
        return SingleTransformer { upstream: Single<T> ->
            upstream
                .doOnSubscribe { doOnSubscribe(method) }
                .doOnSuccess { doOnComplete(method) }
                .doOnError { onError(method, it) }
        }
    }

    override fun <T> applyObservableMetrics(method: String): ObservableTransformer<T, T> {
        return ObservableTransformer { observer: Observable<T> ->
            observer
                .doOnSubscribe { doOnSubscribe(method) }
                .doOnComplete { doOnComplete(method) }
                .doOnNext { doOnNext(method) }
                .doOnError { onError(method, it) }
        }
    }

    override fun <T> applyFlowableMetrics(method: String): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream: Flowable<T> ->
            upstream
                .doOnSubscribe { doOnSubscribe(method) }
                .doOnComplete { doOnComplete(method) }
                .doOnNext { doOnNext(method) }
                .doOnError { onError(method, it) }
        }
    }

    override fun applyCompletableMetrics(method: String): CompletableTransformer {
        return CompletableTransformer { upstream: Completable ->
            upstream
                .doOnSubscribe { doOnSubscribe(method) }
                .doOnComplete { doOnComplete(method) }
                .doOnError { onError(method, it) }
        }
    }

    private fun doOnNext(method: String) {
        metric.onTechNetworkLog(method, "doOnNext")
    }

    private fun doOnComplete(method: String) {
        metric.onTechNetworkLog(method, "doOnComplete")
    }

    private fun doOnSubscribe(method: String) {
        metric.onTechNetworkLog(method, "doOnSubscribe")
    }

    private fun onError(method: String, it: Throwable) {
        metric.onTechNetworkLog(method, it.toString())
    }

}