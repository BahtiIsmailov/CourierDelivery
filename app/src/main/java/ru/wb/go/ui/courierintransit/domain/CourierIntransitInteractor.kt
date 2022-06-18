package ru.wb.go.ui.courierintransit.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierIntransitInteractor : BaseServiceInteractor {

    fun getOffices(): Flow<List<LocalOfficeEntity>>

    fun observeOrderTimer():  Flow<Long>

    suspend fun completeDelivery(order: LocalOrderEntity)

    suspend fun setIntransitTask(orderId: String, boxes: List<LocalBoxEntity>)

    suspend fun clearLocalTaskData()

    fun observeMapAction(): Flow<CourierMapAction>

    fun mapState(state: CourierMapState)

    suspend fun getOrder(): LocalOrderEntity
    suspend fun getOrderId(): String
    suspend fun getOfflineBoxes(): List<LocalBoxEntity>
    suspend fun getBoxes(): List<LocalBoxEntity>
}

/*
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
 */