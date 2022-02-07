package ru.wb.go.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierWarehousesInteractor {

    fun getServerWarehouses(): Single<List<CourierWarehouseLocalEntity>>

    fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    fun observeSearch(): Observable<String>

    fun loadProgress(): Completable

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun isDemoMode(): Boolean

    fun dynamicUrl(): Single<ResponseBody>

}