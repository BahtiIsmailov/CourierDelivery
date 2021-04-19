package com.wb.logistics.network.api.app

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.boxtoflight.ScannedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppRepository {

    //==============================================================================================

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
    fun saveBoxScanned(flightBoxScannedEntity: ScannedBoxEntity): Completable

    fun observeBoxesScanned(): Flowable<List<ScannedBoxEntity>>

    fun deleteBoxScanned(flightBoxScannedEntity: ScannedBoxEntity): Completable

    fun findBoxScanned(barcode: String): Single<SuccessOrEmptyData<ScannedBoxEntity>>

    fun loadBoxScanned(barcodes: List<String>): Single<List<ScannedBoxEntity>>

    fun deleteAllBoxScanned()

    fun groupByDstAddressBoxScanned(): Single<List<ScannedBoxGroupByAddressEntity>>

    //==============================================================================================
    //balance await
    //==============================================================================================

    fun saveFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: ScannedBoxBalanceAwaitEntity): Completable

    fun observeFlightBoxBalanceAwait(): Flowable<List<ScannedBoxBalanceAwaitEntity>>

    fun flightBoxBalanceAwait(): Single<List<ScannedBoxBalanceAwaitEntity>>

    fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: ScannedBoxBalanceAwaitEntity): Completable

    fun deleteAllFlightBoxBalanceAwait()

}