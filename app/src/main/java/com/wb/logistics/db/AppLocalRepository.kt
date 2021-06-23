package com.wb.logistics.db

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.dcunloadedboxes.*
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxByAddressEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppLocalRepository {

    fun saveFlightAndOffices(
        flightEntity: FlightEntity, flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable

    fun changeFlightOfficeUnloading(
        dstOfficeId: Int,
        isUnloading: Boolean,
        notUnloadingCause: String,
    ): Completable

    fun findFlightOfficeOptional(id: Int): Single<Optional<FlightOfficeEntity>>

    fun findFlightOffice(id: Int): Single<FlightOfficeEntity>

    fun observeFlightData(): Flowable<FlightData>

    fun observeFlightDataOptional(): Flowable<Optional<FlightData>>

    fun readFlightOptional(): Single<Optional<FlightEntity>>

    fun readFlight(): Single<FlightEntity>

    fun readFlightId(): Single<String>

    fun readFlightDataOptional(): Single<Optional<FlightData>>

    fun deleteAllFlight()

    //==============================================================================================
    //flight boxes
    //==============================================================================================
    fun saveFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    fun saveFlightBox(flightBoxEntity: FlightBoxEntity): Completable

    fun findFlightBox(barcode: String): Single<Optional<FlightBoxEntity>>

    fun deleteAllFlightBoxes()

    fun deleteReturnFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    fun findUnloadedFlightBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun findReturnedFlightBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun observeReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun findReturnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    //==============================================================================================
    //warehouse matching boxes
    //==============================================================================================
    fun saveWarehouseMatchingBoxes(warehouseMatchingBoxes: List<WarehouseMatchingBoxEntity>): Completable

    fun saveWarehouseMatchingBox(warehouseMatchingBox: WarehouseMatchingBoxEntity): Completable

    fun deleteWarehouseMatchingBox(warehouseMatchingBox: WarehouseMatchingBoxEntity): Completable

    fun findWarehouseMatchingBox(barcode: String): Single<Optional<WarehouseMatchingBoxEntity>>

    fun deleteAllWarehouseMatchingBox()

    //==============================================================================================
    //pvz matching boxes
    //==============================================================================================
    fun savePvzMatchingBoxes(pvzMatchingBoxes: List<PvzMatchingBoxEntity>): Completable

    fun savePvzMatchingBox(pvzMatchingBox: PvzMatchingBoxEntity): Completable

    fun readPvzMatchingBoxes(): Single<List<PvzMatchingBoxEntity>>

    fun deletePvzMatchingBox(pvzMatchingBox: PvzMatchingBoxEntity): Completable

    fun findPvzMatchingBox(barcode: String): Single<Optional<PvzMatchingBoxEntity>>

    fun deleteAllPvzMatchingBox()

    //==============================================================================================
    //attached box
    //==============================================================================================
    fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    fun saveAttachedBoxes(attachedBoxesEntity: List<AttachedBoxEntity>): Completable

    fun findAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>>

    fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>>

    fun observeAttachedBoxes(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>>

    fun readAttachedBoxes(): Single<List<AttachedBoxEntity>>

    fun findAttachedBox(barcode: String): Single<Optional<AttachedBoxEntity>>

    fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    fun deleteAttachedBoxes(attachedBoxesEntity: List<AttachedBoxEntity>): Completable

    fun groupAttachedBoxByDstAddress(): Single<List<AttachedBoxGroupByOfficeEntity>>

    fun groupAttachedBox(): Single<AttachedBoxResultEntity>

    fun deleteAllAttachedBox()

    //==============================================================================================
    //unloaded box
    //==============================================================================================
    @Deprecated("")
    fun saveUnloadedBox(unloadedBoxEntity: UnloadedBoxEntity): Completable

    @Deprecated("")
    fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>>

    @Deprecated("")
    fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>>

    @Deprecated("")
    fun findUnloadedBox(barcode: String): Single<Optional<UnloadedBoxEntity>>

    @Deprecated("")
    fun deleteAllUnloadedBox()

    //==============================================================================================
    //attached box
    //==============================================================================================
    fun saveDcUnloadedBox(dcUnloadedBoxEntity: DcUnloadedBoxEntity): Completable

    fun saveDcUnloadedReturnBox(dcUnloadedReturnBoxEntity: DcUnloadedReturnBoxEntity): Completable

    fun findDcUnloadedBox(barcode: String): Single<Optional<DcUnloadedBoxEntity>>

    fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>>

    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>>

    fun observeDcUnloadingScanBox(): Flowable<DcUnloadingScanBoxEntity>

    fun congratulation(): Single<DcCongratulationEntity>

    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

    fun deleteAllDcUnloadedBox()

    //==============================================================================================
    //return box
    //==============================================================================================

    @Deprecated("")
    fun saveReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

    @Deprecated("")
    fun observedReturnBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>>

    @Deprecated("")
    fun findReturnBox(barcode: String): Single<Optional<ReturnBoxEntity>>

    @Deprecated("")
    fun findReturnBoxes(barcodes: List<String>): Single<List<ReturnBoxEntity>>

    @Deprecated("")
    fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

    @Deprecated("")
    fun deleteReturnBoxes(returnBoxesEntity: List<ReturnBoxEntity>): Completable

    @Deprecated("")
    fun groupByDstAddressReturnBox(dstOfficeId: Int): Single<List<ReturnBoxByAddressEntity>>

    @Deprecated("")
    fun deleteAllReturnBox()

    //==============================================================================================

    fun deleteAll()

}