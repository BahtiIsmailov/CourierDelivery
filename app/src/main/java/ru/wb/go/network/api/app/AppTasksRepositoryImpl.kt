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
        autentificatorIntercept.initNameOfMethod("courierWarehouses")
        return remoteRepo.freeTasksOffices(apiVersion()).data
            .map {
                convertCourierWarehouseEntity(it) // сюда пришел ширина и долгота

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
        autentificatorIntercept.initNameOfMethod("courierOrders")
        return withContext(Dispatchers.IO){
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


    private fun convertCourierOrderEntity(courierOrderResponse: CourierOrderResponse): CourierOrderEntity {
        val dstOffices = mutableListOf<CourierOrderDstOfficeEntity>()
        courierOrderResponse.dstOffices.forEach { dstOffice ->
            if (dstOffice.id != -1) {
                dstOffices.add(toCourierOrderDstOfficeEntity(dstOffice))
            }//широта долгота +
        }
        return toCourierOrderEntity(courierOrderResponse, dstOffices)

    }

    private fun apiVersion() =
        if (tokenManager.isContains()) tokenManager.apiVersion() else tokenManager.apiDemoVersion()

}

/*

    private fun convertCourierOrderEntity(courierOrderResponse: CourierOrderResponse): CourierOrderEntity {
        val dstOffices = mutableListOf<CourierOrderDstOfficeEntity>()
        courierOrderResponse.dstOffices.forEach { dstOffice ->
            // TODO: 05.10.2021 убрать после исправлениня на беке получение минусового id
            if (dstOffice.id != -1) {
                dstOffices.add(
                    CourierOrderDstOfficeEntity(
                        id = dstOffice.id,
                        name = dstOffice.name ?: "",
                        fullAddress = dstOffice.fullAddress ?: "",
                        long = dstOffice.long,
                        lat = dstOffice.lat,
                        workTimes = dstOffice.wrkTime ?: "",
                        isUnusualTime = dstOffice.unusualTime
                    )
                )
            }
        }
        return with(courierOrderResponse) {
            CourierOrderEntity(
                id = id,
                routeID = routeID ?: 0,
                gate = gate ?: "",
                minPrice = minPrice,
                minVolume = minVolume,
                minBoxesCount = minBoxesCount,
                dstOffices = dstOffices,
                reservedAt = "",
                reservedDuration = reservedDuration,
                route = route ?: "не указан"
            )
        }
    }

    private fun apiVersion() =
        if (tokenManager.isContains()) tokenManager.apiVersion() else tokenManager.apiDemoVersion()

}

 */