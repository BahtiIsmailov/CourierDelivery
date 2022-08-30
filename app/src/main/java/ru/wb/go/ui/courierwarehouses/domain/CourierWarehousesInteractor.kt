package ru.wb.go.ui.courierwarehouses.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse
import ru.wb.go.network.api.app.remote.courier.TaskBoxCountResponse
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierWarehousesInteractor : BaseServiceInteractor {

    suspend fun getWarehouses(): CourierWarehousesResponse

    suspend fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity)

    suspend fun saveWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity)

    suspend fun deleteWarehouses()

    fun loadProgress()

    fun observeMapAction(): Flow<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun isDemoMode(): Boolean

    fun mapAction(action: CourierMapAction)

    suspend fun loadWarehousesFromId(id:Int):List<CourierWarehouseLocalEntity>

    suspend fun freeOrdersLocalClearAndSave(srcOfficeID: Int): List<CourierOrderLocalDataEntity>

    fun freeOrdersLocal(): Flow<List<CourierOrderLocalDataEntity>>

    fun saveRowOrder(rowOrder: Int)

    suspend fun selectedOrder(rowOrder: Int): CourierOrderLocalDataEntity

    fun selectedRowOrder(): Int

    fun carNumberIsConfirm(): Boolean

    fun carNumber(): String

    fun carType(): Int

    suspend fun anchorTask()

    suspend fun courierLocalOrderEntity(): LocalOrderEntity

    suspend fun getBoxCountWithRidMask(it: CourierOrderLocalEntity) : TaskBoxCountResponse

}

