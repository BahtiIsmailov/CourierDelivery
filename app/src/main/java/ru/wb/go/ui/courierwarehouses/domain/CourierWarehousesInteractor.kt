package ru.wb.go.ui.courierwarehouses.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse
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

    fun clearCacheMutableSharedFlow()

    suspend fun loadWarehousesFromId(id:Int):List<CourierWarehouseLocalEntity>

}

