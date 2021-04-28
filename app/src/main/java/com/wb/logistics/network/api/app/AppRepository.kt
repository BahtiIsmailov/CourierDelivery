package com.wb.logistics.network.api.app

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByAddressEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
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

    // TODO: 28.04.2021 добавить дату и время
    fun saveBoxScannedToBalanceRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    //==============================================================================================
    //attached
    //==============================================================================================
    fun saveAttachedBox(flightBoxScannedEntity: AttachedBoxEntity): Completable

    fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>>

    fun observedAttachedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>>

    fun readAttached(): Single<List<AttachedBoxEntity>>

    fun deleteAttachedBox(flightBoxScannedEntity: AttachedBoxEntity): Completable

    fun findAttachedBox(barcode: String): Single<SuccessOrEmptyData<AttachedBoxEntity>>

    fun loadAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>>

    fun deleteAllAttachedBoxes()

    fun groupAttachedBoxesByDstAddress(): Single<List<AttachedBoxGroupByAddressEntity>>

    //==============================================================================================
    //unloading
    //==============================================================================================
    fun saveUnloadedBox(unloadedBoxEntity: UnloadedBoxEntity): Completable

    fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>>

    fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>>

    fun findUnloadedBox(barcode: String): Single<SuccessOrEmptyData<UnloadedBoxEntity>>

    //==============================================================================================
    //balance await
    //==============================================================================================

    fun saveFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: AttachedBoxBalanceAwaitEntity): Completable

    fun observeFlightBoxBalanceAwait(): Flowable<List<AttachedBoxBalanceAwaitEntity>>

    fun flightBoxBalanceAwait(): Single<List<AttachedBoxBalanceAwaitEntity>>

    fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: AttachedBoxBalanceAwaitEntity): Completable

    fun deleteAllFlightBoxBalanceAwait()


    //==============================================================================================
    //return box
    //==============================================================================================
    fun saveReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

    fun observedReturnBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>>

    fun findReturnBox(barcode: String): Single<SuccessOrEmptyData<ReturnBoxEntity>>

//    fun findReturnBoxesByDstOfficeId(dstOfficeId: Int): Single<ReturnBoxByDstOfficeIdEntity>

}