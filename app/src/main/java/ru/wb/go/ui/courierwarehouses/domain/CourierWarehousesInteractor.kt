package ru.wb.go.ui.courierwarehouses.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierWarehousesInteractor : BaseServiceInteractor {

    suspend fun getWarehouses(): List<CourierWarehouseLocalEntity>

      fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity)

    suspend fun loadProgress()

    fun observeMapAction(): Flow<CourierMapAction>

    fun mapState(state: CourierMapState)

    suspend fun isDemoMode(): Boolean

      fun mapAction(action: CourierMapAction)

}