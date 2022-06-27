package ru.wb.go.db

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class IntransitTimeRepositoryImpl : IntransitTimeRepository {

    //    private val timerStates: BehaviorSubject<Long> = BehaviorSubject.create()
//private var timerDisposable: Disposable? = null
    private val timerState = MutableSharedFlow<Long>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private var timerDisposable: CoroutineScope? = null


    //    override fun startTimer(): Flowable<Long> {
//        if (timerDisposable == null) {
//            timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
//                .scan(0L) { accumulator, _ -> accumulator + 1 }
//                .subscribe({ timerStates.onNext(it) }) { }
//        }
//        return timerStates.toFlowable(BackpressureStrategy.BUFFER)
//    }
    override fun startTimer(): Flow<Long> {
        if (timerDisposable == null) {
            timerDisposable = CoroutineScope(SupervisorJob())
             timerDisposable?.launch(Dispatchers.Default) {
                 var count = 0L
                 while (isActive) {
                     delay(1000)
                     timerState.tryEmit(++count)
                 }
             }

        }
        return timerState

    }
//
//    override fun startTimer(): Flowable<Long> {
//        if (timerDisposable == null) {
//            timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
//                .scan(0L) { accumulator, _ -> accumulator + 1 }
//                .subscribe({ timerStates.onNext(it) }) { }
//        }
//        return timerStates.toFlowable(BackpressureStrategy.BUFFER)
//    }

    override fun stopTimer() {
        timerDisposable?.cancel()
        timerDisposable = null
    }

}