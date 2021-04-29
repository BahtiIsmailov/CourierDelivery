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

    fun loadBoxToBalanceRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    fun removeBoxFromFlightRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        idOffice: Int,
    ): Completable

    fun removeBoxFromBalanceRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable

    //==============================================================================================

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun updateFlightAndTime(): Completable

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
    //attached
    //==============================================================================================
    fun saveAttachedBox(flightBoxScannedEntity: AttachedBoxEntity): Completable

    fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>>

    fun observedAttachedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>>

    fun readAllAttachedBoxes(): Single<List<AttachedBoxEntity>>

    fun deleteAttachedBox(flightBoxScannedEntity: AttachedBoxEntity): Completable

    fun findAttachedBox(barcode: String): Single<SuccessOrEmptyData<AttachedBoxEntity>>

    fun findAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>>

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

    fun findReturnBoxes(barcodes: List<String>): Single<List<ReturnBoxEntity>>

    fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

    //==============================================================================================
    //time
    //==============================================================================================

    fun getOffsetLocalTime(): String

}