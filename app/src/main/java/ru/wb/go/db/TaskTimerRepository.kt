package ru.wb.go.db

import io.reactivex.Flowable
import ru.wb.go.ui.auth.signup.TimerState

interface TaskTimerRepository {

    fun startTimer(durationTime: Int, arrivalTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

}