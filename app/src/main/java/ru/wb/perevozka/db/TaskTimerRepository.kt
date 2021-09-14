package ru.wb.perevozka.db

import io.reactivex.Flowable
import ru.wb.perevozka.ui.auth.signup.TimerState

interface TaskTimerRepository {

    fun startTimer(durationTime: Int, arrivalTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

}