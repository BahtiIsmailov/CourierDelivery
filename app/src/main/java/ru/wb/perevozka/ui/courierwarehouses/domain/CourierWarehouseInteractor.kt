package ru.wb.perevozka.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierWarehouseEntity

interface CourierWarehouseInteractor {

    fun warehouses(): Single<List<CourierWarehouseEntity>>

    fun observeSearch(): Observable<String>

    fun loadProgress(): Completable

}