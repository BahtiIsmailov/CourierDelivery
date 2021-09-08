package ru.wb.perevozka.ui.courierordertimer.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.ui.auth.signup.TimerState

interface CourierOrderTimerInteractor {

    fun anchorTask(): Single<CourierAnchorEntity>

    fun deleteTask(): Completable

    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

}