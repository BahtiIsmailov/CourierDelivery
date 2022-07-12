package ru.wb.go.db

import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.auth.signup.TimerState

interface TaskTimerRepository {

    fun startTimer(durationTime: Int, arrivalTime: Int)
    val timer: Flow<TimerState>
    fun stopTimer()

}