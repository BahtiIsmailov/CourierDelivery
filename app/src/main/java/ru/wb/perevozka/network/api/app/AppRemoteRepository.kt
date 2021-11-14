package ru.wb.perevozka.network.api.app

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import ru.wb.perevozka.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import ru.wb.perevozka.network.api.app.entity.*
import ru.wb.perevozka.network.api.app.entity.boxinfo.BoxInfoDataEntity
import ru.wb.perevozka.network.api.app.entity.warehousescan.WarehouseScanEntity
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusStateEntity
import ru.wb.perevozka.network.api.app.remote.flightstatuses.FlightStatusesResponse
import ru.wb.perevozka.network.api.app.remote.time.TimeResponse

interface AppRemoteRepository {

    fun flight(): Single<FlightDataEntity>

    fun flightsLogs(flightId: Int, createdAt: String, data: String): Completable

    fun warehouseMatchingBoxes(flightId: String): Single<List<WarehouseMatchingBoxEntity>>

    fun pvzMatchingBoxes(flightId: String): Single<List<PvzMatchingBoxEntity>>

    fun boxInfo(barcode: String): Single<BoxInfoDataEntity>

    fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>>

    fun time(): Single<TimeResponse>

    fun loadPvzScan(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable

    fun removeBoxesFromFlight(
        flightId: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
        barcodes: List<String>,
    ): Completable

    fun removeBoxFromWarehouseBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Single<FlightBoxEntity>

    fun unloadPvzScan(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable

    fun flightStatuses(): Single<FlightStatusesResponse>

    fun putFlightStatus(
        flightId: String,
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
        updatedAt: String,
    ): Completable

    fun getFlightStatus(flightId: String): Single<StatusStateEntity>

    fun warehouseScan(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Single<WarehouseScanEntity>

    fun putBoxTracker(
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
        flightId: Int,
        event: String,
    ): Completable


    fun courierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable

    fun courierWarehouses(): Single<List<CourierWarehouseLocalEntity>>

    fun courierOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun tasksMy(): Single<CourierTasksMyEntity>

    fun anchorTask(taskID: String, carNumber: String): Completable

    fun deleteTask(taskID: String): Completable

    fun taskStatuses(taskID: String): Single<CourierTaskStatusesEntity>

    fun taskBoxes(taskID: String): Single<CourierTaskBoxesEntity>

    fun taskStart(taskID: String, courierTaskStartEntity: CourierTaskStartEntity): Completable

    fun taskStatusesReady(
        taskID: String,
        courierTaskStatusesIntransitEntity: List<CourierTaskStatusesIntransitEntity>
    ): Single<CourierTaskStatusesIntransitCostEntity>

    fun taskStatusesIntransit(
        taskID: String,
        courierTaskStatusesIntransitEntity: List<CourierTaskStatusesIntransitEntity>
    ): Completable

    fun taskStatusesEnd(taskID: String): Completable

    fun putCarNumbers(carNumbersEntity: List<CarNumberEntity>): Completable

    fun billing(isShowTransaction: Boolean): Single<BillingCommonEntity>

    fun appVersion(): Single<String>

}