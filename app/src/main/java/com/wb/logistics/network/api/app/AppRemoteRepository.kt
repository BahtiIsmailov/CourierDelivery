package com.wb.logistics.network.api.app

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.network.api.app.remote.flight.FlightRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import io.reactivex.Completable
import io.reactivex.Single

interface AppRemoteRepository {

    fun updateMatchingBoxes(flightId: String): Single<List<MatchingBoxEntity>>

    fun boxInfo(barcode: String): Single<SuccessOrEmptyData<BoxInfoEntity>>

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun flight(): Single<FlightRemote?>

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

    fun putFlightStatus(
        flightID: String,
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean
        ): Completable

    fun getFlightStatus(flightID: String): Single<StatusStateEntity>

}