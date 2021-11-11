package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierOrderInteractor {

    fun orders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun clearAndSaveSelectedOrder(courierOrderEntity: CourierOrderEntity): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}