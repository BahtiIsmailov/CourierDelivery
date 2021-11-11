package ru.wb.go.ui.courierorderconfirm.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.ui.auth.signup.TimerState

interface CourierOrderConfirmInteractor {

    fun anchorTask(): Completable

    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

    fun carNumber(): String

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

}