package com.wb.logistics.data

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.network.api.app.response.flightstatuses.FlightStatusesRemote
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppRepository {

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun updateFlight(): Completable

    fun updateFlightBox(flightId: Int): Completable

    fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>>

    fun readFlight(): Single<SuccessOrEmptyData<FlightEntity>>

    fun readFlightData(): Single<SuccessOrEmptyData<FlightData>>

    fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>>

    fun boxInfo(barcode: String): Single<SuccessOrEmptyData<BoxInfoEntity>>

    //==============================================================================================

    fun boxToFlight(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ): Completable

    fun deleteFlightBoxScannedRemote(
        flightID: String,
        barcode: String,
        isManual: Boolean,
        idOffice: Int,
    ): Completable

    //==============================================================================================
    fun saveFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable

    fun observeFlightBoxesScanned(): Flowable<List<FlightBoxScannedEntity>>

    fun deleteFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable

    fun deleteAllFlightBoxScanned()

    fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<FlightBoxScannedEntity>>

}