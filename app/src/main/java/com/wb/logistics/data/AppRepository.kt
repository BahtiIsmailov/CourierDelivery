package com.wb.logistics.data

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.boxtoflight.FlightBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppRepository {

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun updateFlight(): Completable

    fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>>

    fun readFlight(): Single<SuccessOrEmptyData<FlightEntity>>

    fun readFlightData(): Single<SuccessOrEmptyData<FlightData>>

    fun deleteAllFlightData()

    //==============================================================================================

    fun updateFlightBoxes(flightId: Int): Completable

    fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>>

    fun deleteAllFlightBox()

    //==============================================================================================

    fun boxInfo(barcode: String): Single<SuccessOrEmptyData<BoxInfoEntity>>

    //==============================================================================================

    fun updateMatchingBoxes(flightId: String): Completable

    fun findMatchingBox(barcode: String): Single<SuccessOrEmptyData<MatchingBoxEntity>>

    fun deleteAllMatchingBox()

    //==============================================================================================

    fun flightBoxScannedToBalanceRemote(
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

    fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<FlightBoxScannedEntity>>

    fun loadFlightBoxScanned(barcodes: List<String>): Single<List<FlightBoxScannedEntity>>

    fun deleteAllFlightBoxScanned()

    //==============================================================================================
    //balance await
    //==============================================================================================

    fun saveFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: FlightBoxBalanceAwaitEntity): Completable

    fun observeFlightBoxBalanceAwait(): Flowable<List<FlightBoxBalanceAwaitEntity>>

    fun flightBoxBalanceAwait(): Single<List<FlightBoxBalanceAwaitEntity>>

    fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: FlightBoxBalanceAwaitEntity): Completable

    fun deleteAllFlightBoxBalanceAwait()

}