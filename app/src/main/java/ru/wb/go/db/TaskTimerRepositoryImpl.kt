package ru.wb.go.db

import io.reactivex.Observable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import ru.wb.go.utils.CoroutineInterval
import java.util.concurrent.TimeUnit

class TaskTimerRepositoryImpl : TaskTimerRepository {

    //private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    private val timerStates = MutableSharedFlow<TimerState>()
    //private var timerDisposable: Disposable? = null
    private var durationTime = 0
    private var arrivalTime = 0

    override suspend fun startTimer(durationTime: Int, arrivalTime: Int) {
        this.durationTime = durationTime
        this.arrivalTime = arrivalTime
        coroutineScope {
            CoroutineInterval.interval(1000L,TimeUnit.MILLISECONDS)
                .onEach {
                    onTimeConfirmCode(it)
                }
                .launchIn(this)
            publishCallState(TimerStateImpl(durationTime,arrivalTime))
        }
    }
//    override suspend fun startTimer(durationTime: Int, arrivalTime: Int) {
//        this.durationTime = durationTime
//        this.arrivalTime = arrivalTime
//        if (timerDisposable == null) {
//            timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
//                .subscribe({ onTimeConfirmCode(it) }) { }
//            publishCallState(TimerStateImpl(durationTime, arrivalTime))
//        }
//    }

//    override val timer: Flow<TimerState>
//        get() = timerStates.toFlowable(BackpressureStrategy.BUFFER)

    override val timer: Flow<TimerState>
        get() = timerStates


    override suspend fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    private suspend fun onTimeConfirmCode(tick: Long) {
        if (tick >= arrivalTime) {
            timeConfirmCodeDisposable()
            publishCallState(TimerOverStateImpl())
        } else {
            val downTickSec = arrivalTime - tick.toInt()
            publishCallState(TimerStateImpl(durationTime, downTickSec))
        }
    }

    private suspend fun publishCallState(timerState: TimerState) {
        timerStates.emit(timerState)
    }

    private suspend fun timeConfirmCodeDisposable() {
        durationTime = 0
        arrivalTime = 0
        publishCallState(TimerStateImpl(durationTime, 0))
//        if (timerDisposable != null) {
//            timerDisposable!!.dispose()
//            timerDisposable = null
//        }
    }


}