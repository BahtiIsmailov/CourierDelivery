package ru.wb.go.network.api.app

import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse

interface AppTasksRepository {

    suspend fun courierWarehouses(): CourierWarehousesResponse

    suspend fun getFreeOrders(srcOfficeID: Int): List<CourierOrderEntity>

}