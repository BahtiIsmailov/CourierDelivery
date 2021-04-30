package com.wb.logistics.db

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
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
    //attached box
    //==============================================================================================
    fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    fun findAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>>

    fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>>

    fun observeFilterByOfficeAttachedBoxes(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>>

    fun readAttachedBoxes(): Single<List<AttachedBoxEntity>>

    fun findAttachedBox(barcode: String): Single<SuccessOrEmptyData<AttachedBoxEntity>>

    fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    fun deleteAllAttachedBox()

    fun groupAttachedBoxByDstAddress(): Single<List<AttachedBoxGroupByOfficeEntity>>

    //==============================================================================================
    //attached box
    //==============================================================================================
    fun saveUnloadedBox(unloadedBoxEntity: UnloadedBoxEntity): Completable

    fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>>

    fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>>

    fun findUnloadedBox(barcode: String): Single<SuccessOrEmptyData<UnloadedBoxEntity>>

    //==============================================================================================
    //balance box
    //==============================================================================================
    fun saveFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable

    fun observeFlightBoxBalanceAwait(): Flowable<List<AttachedBoxBalanceAwaitEntity>>

    fun flightBoxBalanceAwait(): Single<List<AttachedBoxBalanceAwaitEntity>>

    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable

    fun deleteAllFlightBoxBalanceAwait()

    //==============================================================================================
    //return box
    //==============================================================================================

    fun saveReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

    fun observedReturnBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>>

    fun findReturnBox(barcode: String): Single<SuccessOrEmptyData<ReturnBoxEntity>>

    fun findReturnBoxes(barcodes: List<String>): Single<List<ReturnBoxEntity>>

    fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

}