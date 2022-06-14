package ru.wb.go.ui.courierordertimer.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.ui.auth.signup.TimerState

interface CourierOrderTimerInteractor {

    suspend fun deleteTask()

    fun startTimer(reservedDuration: String, reservedAt: String)
    val timer: Flow<TimerState>
    fun stopTimer()

    suspend fun observeOrderData(): CourierOrderLocalDataEntity

    suspend fun timerEntity():  CourierTimerEntity

}