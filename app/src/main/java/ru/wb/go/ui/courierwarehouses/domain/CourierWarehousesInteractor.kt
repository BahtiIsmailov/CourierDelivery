package ru.wb.go.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierWarehousesInteractor : BaseServiceInteractor {

    suspend fun getWarehouses():  List<CourierWarehouseLocalEntity>

    suspend fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity)

    suspend fun loadProgress()

    suspend fun observeMapAction(): CourierMapAction

    fun mapState(state: CourierMapState)

    fun isDemoMode(): Boolean

}