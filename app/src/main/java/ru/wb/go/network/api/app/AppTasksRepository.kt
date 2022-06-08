package ru.wb.go.network.api.app

import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity

interface AppTasksRepository {

    fun courierWarehouses(): Single<List<CourierWarehouseLocalEntity>>

    fun getFreeOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

}