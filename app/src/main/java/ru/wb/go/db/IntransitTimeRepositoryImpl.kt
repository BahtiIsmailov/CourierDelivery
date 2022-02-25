package ru.wb.go.db

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class IntransitTimeRepositoryImpl : IntransitTimeRepository {

    private val timerStates: BehaviorSubject<Long> = BehaviorSubject.create()
    private var timerDisposable: Disposable? = null

    override fun startTimer(): Flowable<Long> {
        if (timerDisposable == null) {
            timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
                .scan(0L) { accumulator, _ -> accumulator + 1 }
                .subscribe({ timerStates.onNext(it) }) { }
        }
        return timerStates.toFlowable(BackpressureStrategy.BUFFER)
    }

    override fun stopTimer() {
        timeDisposable()
    }

    private fun timeDisposable() {
        if (timerDisposable != null) {
            timerDisposable!!.dispose()
            timerDisposable = null
        }
    }

}