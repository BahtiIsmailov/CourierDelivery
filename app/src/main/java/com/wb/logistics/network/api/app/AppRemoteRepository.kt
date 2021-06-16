package com.wb.logistics.network.api.app

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import io.reactivex.Completable
import io.reactivex.Single

interface AppRemoteRepository {

    fun flight(): Single<FlightDataEntity>

    fun matchingBoxes(flightId: String): Single<List<MatchingBoxEntity>>

    fun boxInfo(barcode: String): Single<Optional<BoxInfoEntity>>

    fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>>

    fun time(): Single<TimeRemote>

    fun warehouseBoxToBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    fun pvzBoxToBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
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
        currentOffice: Int,
    ): Completable

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun putFlightStatus(
        flightId: String,
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
        updatedAt: String,
    ): Completable

    fun getFlightStatus(flightID: String): Single<StatusStateEntity>

}