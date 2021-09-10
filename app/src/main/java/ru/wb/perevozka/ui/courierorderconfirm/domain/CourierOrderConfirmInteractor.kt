package ru.wb.perevozka.ui.courierorderconfirm.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.ui.auth.signup.TimerState

interface CourierOrderConfirmInteractor {

    fun anchorTask(): Completable

    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

    fun carNumber(): String

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

}