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

    suspend fun setIntransitTask(orderId: String,srcOfficeID:Int, boxes: List<LocalBoxEntity>)

    suspend fun clearLocalTaskData()

    fun observeMapAction(): Flow<CourierMapAction>

    fun mapState(state: CourierMapState)

    suspend fun getOrder(): LocalOrderEntity?
    suspend fun getOrderId(): String
    suspend fun getOfflineBoxes(): List<LocalBoxEntity>
    suspend fun getBoxes(): List<LocalBoxEntity>
    suspend fun getSrcOfficeID():Int?
}

