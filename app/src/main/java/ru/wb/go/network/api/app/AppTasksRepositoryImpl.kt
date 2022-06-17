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


//       override suspend fun courierWarehouses(): List<CourierWarehouseLocalEntity>  {
//    return with(Dispatchers.IO){
//        autentificatorIntercept.initNameOfMethod("courierWarehouses")
//         remoteRepo.freeTasksOffices(apiVersion()).data
//         .map {
//            convertCourierWarehouseEntity(it)
//        }.toList()
//    }
//}


//    private fun convertCourierWarehouseEntity(courierOfficeResponse: CourierWarehouseResponse): CourierWarehouseLocalEntity {
//        return with(courierOfficeResponse) {
//            CourierWarehouseLocalEntity(
//                id = id,
//                name = name,
//                fullAddress = fullAddress,
//                longitude = long,
//                latitude = lat
//            )
//        }
//    }


    override suspend fun getFreeOrders(srcOfficeID: Int): List<CourierOrderEntity> {
        return withContext(Dispatchers.IO){
            autentificatorIntercept.initNameOfMethod("courierOrders")
            remoteRepo.freeTasks(apiVersion(), srcOfficeID).data.map {
                convertCourierOrderEntity(it)
            }.toList()
        }
    }

//    override suspend fun getFreeOrders(srcOfficeID: Int):  List<CourierOrderEntity>  {
//        autentificatorIntercept.initNameOfMethod("courierOrders")
//        return remoteRepo.freeTasks(apiVersion(), srcOfficeID).data.map {
//                order -> convertCourierOrderEntity(order)
//        }.toList()
//    }

    private fun apiVersion() =
        if (tokenManager.isContains()) tokenManager.apiVersion() else tokenManager.apiDemoVersion()

}