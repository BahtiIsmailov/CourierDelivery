package ru.wb.go.db

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.wb.go.utils.CoroutineExtension
import java.util.concurrent.TimeUnit

class IntransitTimeRepositoryImpl : IntransitTimeRepository {

//    private val timerStates: BehaviorSubject<Long> = BehaviorSubject.create()
//private var timerDisposable: Disposable? = null
    private val timerState = MutableSharedFlow<Long>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val coroutineScope = CoroutineScope(SupervisorJob())




//    override fun startTimer(): Flowable<Long> {
//        if (timerDisposable == null) {
//            timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
//                .scan(0L) { accumulator, _ -> accumulator + 1 }
//                .subscribe({ timerStates.onNext(it) }) { }
//        }
//        return timerStates.toFlowable(BackpressureStrategy.BUFFER)
//    }
    override fun startTimer():Long {
    coroutineScope.launch {
        CoroutineExtension.interval(1000L, TimeUnit.MILLISECONDS)
          timerState
            .scan(0L) { accumulator, _ -> accumulator + 1 }
            .onEach { timerState.tryEmit(it) }
            .launchIn(this)
    }
    var long = 0L
    timerState.onEach {
        long = it
    }
     return long

}

    override fun stopTimer() {
        coroutineScope.cancel()
    }

}