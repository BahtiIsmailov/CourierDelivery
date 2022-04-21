package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierIntransitInteractor : BaseServiceInteractor {

    fun getOffices(): Observable<List<LocalOfficeEntity>>

    fun observeOrderTimer(): Observable<Long>

    fun completeDelivery(order: LocalOrderEntity): Completable

    fun setIntransitTask(orderId: String, boxes: List<LocalBoxEntity>): Completable

    fun clearLocalTaskData()

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun getOrder(): LocalOrderEntity
    fun getOrderId(): String
    fun getOfflineBoxes(): List<LocalBoxEntity>
    fun getBoxes(): List<LocalBoxEntity>
}