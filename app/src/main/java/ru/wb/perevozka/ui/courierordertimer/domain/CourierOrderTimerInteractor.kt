package ru.wb.perevozka.ui.courierordertimer.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.ui.auth.signup.TimerState

interface CourierOrderTimerInteractor {

    fun anchorTask(taskID: String): Single<CourierAnchorEntity>

    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

}