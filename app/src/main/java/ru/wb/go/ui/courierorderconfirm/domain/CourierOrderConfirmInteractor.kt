package ru.wb.go.ui.courierorderconfirm.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.auth.signup.TimerState

interface CourierOrderConfirmInteractor {

    fun anchorTask(): Completable

    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()

    fun carNumber(): String

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun observeNetworkConnected(): Observable<NetworkState>

}