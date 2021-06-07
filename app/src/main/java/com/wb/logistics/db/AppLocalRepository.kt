package com.wb.logistics.db

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.dcunloadedboxes.*
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxByAddressEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppLocalRepository {

    fun saveFlight(
        flightEntity: FlightEntity, flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable

    fun changeFlightOfficeUnloading(dstOfficeId: Int, isUnloading: Boolean, notUnloadingCause: String): Completable

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

    fun findMatchingBox(barcode: String): Single<SuccessOrEmptyData<MatchingBoxEntity>>

    fun deleteAllMatchingBox()

    //==============================================================================================
    //attached box
    //==============================================================================================
    fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    fun findAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>>

    fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>>

    fun observeAttachedBoxes(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>>

    fun readAttachedBoxes(): Single<List<AttachedBoxEntity>>

    fun findAttachedBox(barcode: String): Single<SuccessOrEmptyData<AttachedBoxEntity>>

    fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    fun groupAttachedBoxByDstAddress(): Single<List<AttachedBoxGroupByOfficeEntity>>

    fun groupAttachedBox(): Single<AttachedBoxResultEntity>

    fun deleteAllAttachedBox()

    //==============================================================================================
    //unloaded box
    //==============================================================================================
    fun saveUnloadedBox(unloadedBoxEntity: UnloadedBoxEntity): Completable

    fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>>

    fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>>

    fun findUnloadedBox(barcode: String): Single<SuccessOrEmptyData<UnloadedBoxEntity>>

    fun deleteAllUnloadedBox()

    //==============================================================================================
    //attached box
    //==============================================================================================
    fun saveDcUnloadedBox(dcUnloadedBoxEntity: DcUnloadedBoxEntity): Completable

    fun saveDcUnloadedReturnBox(dcUnloadedReturnBoxEntity: DcUnloadedReturnBoxEntity): Completable

    fun findDcUnloadedBox(barcode: String): Single<SuccessOrEmptyData<DcUnloadedBoxEntity>>

    fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>>

    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>>

    fun observeDcUnloadingScanBox(): Flowable<DcUnloadingScanBoxEntity>

    fun congratulation(): Single<DcCongratulationEntity>

    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

    fun deleteAllDcUnloadedBox()

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

    fun groupByDstAddressReturnBox(dstOfficeId: Int): Single<List<ReturnBoxByAddressEntity>>

    fun deleteAllReturnBox()

    //==============================================================================================

    fun deleteAll()

}