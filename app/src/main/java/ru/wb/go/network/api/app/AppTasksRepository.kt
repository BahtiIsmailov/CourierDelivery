package ru.wb.go.network.api.app

import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse
import ru.wb.go.network.api.app.remote.courier.TaskBoxCountResponse

interface AppTasksRepository {

    suspend fun courierWarehouses(): CourierWarehousesResponse

    suspend fun getFreeOrders(srcOfficeID: Int,isDemo:Boolean): List<CourierOrderEntity>

    suspend fun getBoxCountWithRidMask(ridMask:Long):TaskBoxCountResponse

}