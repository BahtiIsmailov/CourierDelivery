package ru.wb.perevozka.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.ui.couriermap.CourierMapAction
import ru.wb.perevozka.ui.couriermap.CourierMapState

interface CourierWarehouseInteractor {

    fun warehouses(): Single<List<CourierWarehouseLocalEntity>>

    fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    fun observeSearch(): Observable<String>

    fun loadProgress(): Completable

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

}