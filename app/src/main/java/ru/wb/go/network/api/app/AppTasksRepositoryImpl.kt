package ru.wb.go.network.api.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.remote.courier.CourierOrderResponse
import ru.wb.go.network.api.app.remote.courier.CourierWarehouseResponse
import ru.wb.go.network.token.TokenManager

class AppTasksRepositoryImpl(
    private val autentificatorIntercept: AutentificatorIntercept,
    private val remoteRepo: AppTasksApi,
    private val tokenManager: TokenManager
) : AppTasksRepository {


    override suspend fun courierWarehouses(): List<CourierWarehouseLocalEntity> {
        return withContext(Dispatchers.IO) {
            autentificatorIntercept.initNameOfMethod("courierWarehouses")
            remoteRepo.freeTasksOffices(apiVersion()).data
                .map {
                    convertCourierWarehouseEntity(it) // сюда пришел ширина и долгота
                }
        }
    }


    override suspend fun getFreeOrders(srcOfficeID: Int): List<CourierOrderEntity> {
        return withContext(Dispatchers.IO){
            autentificatorIntercept.initNameOfMethod("courierOrders")
            remoteRepo.freeTasks(apiVersion(), srcOfficeID).data.map {
                convertCourierOrderEntity(it)
            }.toList()
        }
    }
    /*
        override fun getFreeOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>> {
        return remoteRepo.freeTasks(apiVersion(), srcOfficeID)
            .map { it.data }
            .flatMap {
                Observable.fromIterable(it)
                    .map { order -> convertCourierOrderEntity(order) }
                    .toList()
            }
            .compose(rxSchedulerFactory.applySingleMetrics("courierOrders"))
    }
     */

    private fun apiVersion() =
        if (tokenManager.isContains()) tokenManager.apiVersion() else tokenManager.apiDemoVersion()

}