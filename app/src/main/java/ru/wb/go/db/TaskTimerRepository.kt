package ru.wb.go.db

import io.reactivex.Flowable
import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.auth.signup.TimerState

interface TaskTimerRepository {

    suspend fun startTimer(durationTime: Int, arrivalTime: Int)
    val timer: Flow<TimerState>
    suspend fun stopTimer()

}