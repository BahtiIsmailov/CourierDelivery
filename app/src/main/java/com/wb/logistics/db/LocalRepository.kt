package com.wb.logistics.db

import com.wb.logistics.db.entity.boxtoflight.ScannedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface LocalRepository {

    fun saveFlight(
        flightEntity: FlightEntity, flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable

    fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>>

    fun readFlight(): Single<SuccessOrEmptyData<FlightEntity>>

    fun readFlightData(): Single<SuccessOrEmptyData<FlightData>>

    fun deleteAllFlight()
    //==============================================================================================

    fun saveFlightBoxes(boxesEntity: List<FlightBoxEntity>): Completable

    fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>>

    fun deleteAllFlightBoxes()

    //==============================================================================================
    fun saveMatchingBoxes(matchingBoxes: List<MatchingBoxEntity>): Completable

    fun findMatchBox(barcode: String): Single<SuccessOrEmptyData<MatchingBoxEntity>>

    fun deleteAllMatchingBox()

    //==============================================================================================
    //scanned box
    //==============================================================================================
    fun saveFlightBoxScanned(flightBoxScannedEntity: ScannedBoxEntity): Completable

    fun loadFlightBoxScanned(barcodes: List<String>): Single<List<ScannedBoxEntity>>

    fun observeFlightBoxScanned(): Flowable<List<ScannedBoxEntity>>

    fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<ScannedBoxEntity>>

    fun deleteFlightBoxScanned(flightBoxScannedEntity: ScannedBoxEntity): Completable

    fun deleteAllFlightBoxScanned()

    fun groupByDstAddressScannedBoxScanned(): Single<List<ScannedBoxGroupByAddressEntity>>

    //==============================================================================================
    //balance box
    //==============================================================================================
    fun saveFlightBoxBalanceAwait(flightBoxBalanceEntity: ScannedBoxBalanceAwaitEntity): Completable

    fun observeFlightBoxBalanceAwait(): Flowable<List<ScannedBoxBalanceAwaitEntity>>

    fun flightBoxBalanceAwait(): Single<List<ScannedBoxBalanceAwaitEntity>>

    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: ScannedBoxBalanceAwaitEntity): Completable

    fun deleteAllFlightBoxBalanceAwait()

}