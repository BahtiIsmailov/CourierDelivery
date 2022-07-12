package ru.wb.go.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import ru.wb.go.utils.CoroutineExtension
import java.util.concurrent.TimeUnit

class TaskTimerRepositoryImpl : TaskTimerRepository {

    private val timerStates = MutableStateFlow<TimerState>(TimerStateImpl(0,0))

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
        timerStates.update{
            timerState
        }
    }

    private fun timeConfirmCodeDisposable() {
        durationTime = 0
        arrivalTime = 0
        publishCallState(TimerStateImpl(durationTime, 0))
        if (corutineScope != null) {
            corutineScope!!.cancel()
            corutineScope = null
        }
    }


}