package com.wb.logistics.network.api.app

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import com.wb.logistics.network.api.app.entity.boxinfo.BoxInfoDataEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesResponse
import com.wb.logistics.network.api.app.remote.time.TimeResponse
import io.reactivex.Completable
import io.reactivex.Single

interface AppRemoteRepository {

    fun flight(): Single<FlightDataEntity>

    fun warehouseMatchingBoxes(flightId: String): Single<List<WarehouseMatchingBoxEntity>>

    fun pvzMatchingBoxes(flightId: String): Single<List<PvzMatchingBoxEntity>>

    fun boxInfo(barcode: String): Single<BoxInfoDataEntity>

    fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>>

    fun time(): Single<TimeResponse>

    fun putBoxToPvzBalance(
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
        barcodes: List<String>
    ): Completable

    fun removeBoxFromWarehouseBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable

    fun removeBoxFromPvzBalance(
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
    ): Completable

}