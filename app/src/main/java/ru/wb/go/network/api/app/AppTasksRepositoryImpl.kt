package ru.wb.go.network.api.app

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.remote.courier.CourierOrderResponse
import ru.wb.go.network.api.app.remote.courier.CourierWarehouseResponse
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

class AppTasksRepositoryImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val remoteRepo: AppTasksApi,
    private val tokenManager: TokenManager
) : AppTasksRepository {

    override fun courierWarehouses(): Single<List<CourierWarehouseLocalEntity>> {
        return remoteRepo.freeTasksOffices(apiVersion())
            .map { it.data }
            .flatMap {
                Observable.fromIterable(it)
                    .map { office -> convertCourierWarehouseEntity(office) }
                    .toList()
            }
            .compose(rxSchedulerFactory.applySingleMetrics("courierWarehouses"))
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