package ru.wb.go.db

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import ru.wb.go.utils.CoroutineExtension
import java.util.concurrent.TimeUnit

class TaskTimerRepositoryImpl : TaskTimerRepository {

    //private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    //private var timerDisposable: Disposable? = null

    private val timerStates = MutableSharedFlow<TimerState>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var corutineScope:CoroutineScope? = null

    private var durationTime = 0
    private var arrivalTime = 0


    override fun startTimer(durationTime: Int, arrivalTime: Int) {
        this.durationTime = durationTime
        this.arrivalTime = arrivalTime
        if (corutineScope == null) {
            corutineScope = CoroutineScope(SupervisorJob())
            CoroutineExtension.interval(1000L, TimeUnit.MILLISECONDS)
                .onEach {
                    onTimeConfirmCode(it)
                }
                .launchIn(corutineScope!!)
            publishCallState(TimerStateImpl(durationTime, arrivalTime))
        }
    }
//    override val timer: Flow<TimerState>
//        get() = timerStates.toFlowable(BackpressureStrategy.BUFFER)

    override val timer: Flow<TimerState>
        get() = timerStates


    override fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    private fun onTimeConfirmCode(tick: Long) {
        if (tick >= arrivalTime) {
            timeConfirmCodeDisposable()
            publishCallState(TimerOverStateImpl())
        } else {
            val downTickSec = arrivalTime - tick.toInt()
            publishCallState(TimerStateImpl(durationTime, downTickSec))
        }
    }

    private fun publishCallState(timerState: TimerState) {
        timerStates.tryEmit(timerState)
    }

    private fun timeConfirmCodeDisposable() {
        durationTime = 0
        arrivalTime = 0
        publishCallState(TimerStateImpl(durationTime, 0))
        corutineScope!!.cancel()
        corutineScope = null
//        job?.cancel()

    }


}