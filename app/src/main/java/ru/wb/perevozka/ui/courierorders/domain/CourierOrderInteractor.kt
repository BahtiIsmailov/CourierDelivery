package ru.wb.perevozka.ui.courierorders.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.network.monitor.NetworkState

interface CourierOrderInteractor {

    fun orders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun observeNetworkConnected(): Observable<NetworkState>

}