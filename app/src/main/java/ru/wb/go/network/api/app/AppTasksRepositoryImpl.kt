package ru.wb.go.network.api.app

import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.remote.courier.CourierOrderResponse
import ru.wb.go.network.api.app.remote.courier.CourierWarehouseResponse
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

class AppTasksRepositoryImpl(
    private val autentificatorIntercept: AutentificatorIntercept,
    private val remoteRepo: AppTasksApi,
    private val tokenManager: TokenManager
) : AppTasksRepository {


override suspend fun courierWarehouses(): List<CourierWarehouseLocalEntity>  {
    return withContext(Dispatchers.IO){
        autentificatorIntercept.initNameOfMethod("courierWarehouses")
        remoteRepo.freeTasksOffices(apiVersion()).data
         .map {
            convertCourierWarehouseEntity(it)
        }.toList()
    }
}
    private fun convertCourierWarehouseEntity(courierOfficeResponse: CourierWarehouseResponse): CourierWarehouseLocalEntity {
        return with(courierOfficeResponse) {
            CourierWarehouseLocalEntity(
                id = id,
                name = name,
                fullAddress = fullAddress,
                longitude = long,
                latitude = lat
            )
        }
    }

    override suspend fun getFreeOrders(srcOfficeID: Int): List<CourierOrderEntity>  {
        autentificatorIntercept.initNameOfMethod("courierOrders")
           return remoteRepo.freeTasks(apiVersion(),srcOfficeID).data.map {
               convertCourierOrderEntity(it)
           }
    }
//    override fun getFreeOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>> {
//        return remoteRepo.freeTasks(apiVersion(), srcOfficeID)
//            .map { it.data }
//            .flatMap {
//                Observable.fromIterable(it)
//                    .map { order -> convertCourierOrderEntity(order) }
//                    .toList()
//            }
//            .compose(rxSchedulerFactory.applySingleMetrics("courierOrders"))
//    }

    private fun convertCourierOrderEntity(courierOrderResponse: CourierOrderResponse): CourierOrderEntity {
        val dstOffices = mutableListOf<CourierOrderDstOfficeEntity>()
        courierOrderResponse.dstOffices.forEach { dstOffice ->
            if (dstOffice.id != -1) {
                dstOffices.add(toCourierOrderDstOfficeEntity(dstOffice))
            }//широта долгота +
        }
        return toCourierOrderEntity(courierOrderResponse,dstOffices)

    }

    private fun apiVersion() =
        if (tokenManager.isContains()) tokenManager.apiVersion() else tokenManager.apiDemoVersion()

}