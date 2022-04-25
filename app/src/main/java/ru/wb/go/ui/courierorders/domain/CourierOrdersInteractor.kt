package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrdersInteractor : BaseServiceInteractor {

    fun getFreeOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun clearAndSaveSelectedOrder(courierOrderEntity: CourierOrderEntity): Completable

    fun mapState(state: CourierMapState)

    fun observeMapAction(): Observable<CourierMapAction>

    fun carNumberIsConfirm(): Boolean

    fun isDemoMode(): Boolean

    fun carNumber(): String

    fun anchorTask(orderEntity: CourierOrderEntity): Completable

}