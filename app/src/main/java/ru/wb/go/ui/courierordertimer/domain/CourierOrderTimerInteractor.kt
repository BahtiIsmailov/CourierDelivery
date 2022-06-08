package ru.wb.go.ui.courierordertimer.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.ui.auth.signup.TimerState

interface CourierOrderTimerInteractor {

    suspend fun deleteTask()

    fun startTimer(reservedDuration: String, reservedAt: String)
    val timer: Flowable<TimerState>
    fun stopTimer()

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    suspend fun timerEntity():  CourierTimerEntity

}