package com.wb.logistics.network.api.app

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import com.wb.logistics.network.api.app.entity.boxinfo.BoxInfoDataEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import io.reactivex.Completable
import io.reactivex.Single

interface AppRemoteRepository {

    fun flight(): Single<FlightDataEntity>

    fun warehouseMatchingBoxes(flightId: String): Single<List<WarehouseMatchingBoxEntity>>

    fun boxInfo(barcode: String): Single<BoxInfoDataEntity>

    fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>>

    fun time(): Single<TimeRemote>

    fun pvzBoxToBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable

    fun removeBoxFromFlight(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        officeId: Int,
    ): Completable

    fun removeBoxesFromFlight(
        flightId: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
        barcodes: List<String>
    ): Completable

    fun removeBoxFromBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable

    fun flightStatuses(): Single<FlightStatusesRemote>

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

}