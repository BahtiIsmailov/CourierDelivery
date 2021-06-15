package com.wb.logistics.network.api.app

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.network.api.app.entity.boxinfo.BoxInfoDataEntity
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import io.reactivex.Completable
import io.reactivex.Single

interface AppRemoteRepository {

    fun flight(): Single<FlightDataEntity>

    fun matchingBoxes(flightId: String): Single<List<MatchingBoxEntity>>

    fun boxInfo(barcode: String): Single<BoxInfoDataEntity>

    fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>>

    fun time(): Single<TimeRemote>

    fun warehouseBoxToBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    fun pvzBoxToBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    fun removeBoxFromFlight(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        officeId: Int,
    ): Completable

    fun removeBoxFromBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun putFlightStatus(
        flightID: String,
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
        updatedAt: String,
    ): Completable

    fun getFlightStatus(flightID: String): Single<StatusStateEntity>

}