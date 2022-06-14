package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierIntransitInteractor : BaseServiceInteractor {

    suspend fun getOffices():  List<LocalOfficeEntity>

    suspend fun observeOrderTimer():  Long

    suspend fun completeDelivery(order: LocalOrderEntity)

    suspend fun setIntransitTask(orderId: String, boxes: List<LocalBoxEntity>)

    fun clearLocalTaskData()

    suspend fun observeMapAction(): Flow<CourierMapAction>

    suspend fun mapState(state: CourierMapState)

    suspend fun getOrder(): LocalOrderEntity
    suspend fun getOrderId(): String
    fun getOfflineBoxes(): List<LocalBoxEntity>
    fun getBoxes(): List<LocalBoxEntity>
}