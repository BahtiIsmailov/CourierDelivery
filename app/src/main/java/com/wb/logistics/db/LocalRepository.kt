package com.wb.logistics.db

import com.wb.logistics.db.entity.boxtoflight.FlightBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
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

    fun removeFlight()

    fun saveFlightBoxes(boxesEntity: List<FlightBoxEntity>): Completable

    fun findBoxFromFlight(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>>

    fun removeBoxesFromFlight()

    //==============================================================================================
    //scanned box
    //==============================================================================================
    fun saveFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable

    fun loadFlightBoxScanned(barcodes: List<String>): Single<List<FlightBoxScannedEntity>>

    fun observeFlightBoxScanned(): Flowable<List<FlightBoxScannedEntity>>

    fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<FlightBoxScannedEntity>>

    fun deleteAllFlightBoxScanned()

    fun deleteFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable

    //==============================================================================================
    //balance box
    //==============================================================================================
    fun saveFlightBoxBalanceAwait(flightBoxBalanceEntity: FlightBoxBalanceAwaitEntity): Completable

    fun observeFlightBoxBalanceAwait(): Flowable<List<FlightBoxBalanceAwaitEntity>>

    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: FlightBoxBalanceAwaitEntity): Completable

}