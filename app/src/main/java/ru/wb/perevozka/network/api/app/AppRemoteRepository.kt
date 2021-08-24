package ru.wb.perevozka.network.api.app

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.db.entity.courier.CourierWarehouseEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import ru.wb.perevozka.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
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

    fun courierWarehouses(): Single<List<CourierWarehouseEntity>>

    fun courierOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun anchorTask(taskID: String): Single<CourierAnchorEntity>

}