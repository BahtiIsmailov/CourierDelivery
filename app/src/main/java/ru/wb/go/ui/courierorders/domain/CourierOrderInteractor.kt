package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrderInteractor {

    fun orders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun clearAndSaveSelectedOrder(courierOrderEntity: CourierOrderEntity): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

    fun mapState(state: CourierMapState)

    fun observeMapAction(): Observable<CourierMapAction>

    fun carNumberIsConfirm(): Boolean

}