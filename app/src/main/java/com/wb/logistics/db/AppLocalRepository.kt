package com.wb.logistics.db

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppLocalRepository {

    fun saveFlightAndOffices(
        flightEntity: FlightEntity, flightOfficesEntity: List<FlightOfficeEntity>,
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

    fun findUnloadedFlightBox(
        barcode: String,
        currentOfficeId: Int,
    ): Single<Optional<FlightBoxEntity>>

    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun findReturnedFlightBox(
        barcode: String,
        currentOfficeId: Int,
    ): Single<Optional<FlightBoxEntity>>

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

    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

    fun deleteAllAttachedBox()

    //==============================================================================================
    //attached box
    //==============================================================================================
    fun saveDcUnloadedBox(dcUnloadedBoxEntity: FlightBoxEntity): Completable

    fun saveDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable

    fun findDcReturnHandleBoxes(currentOfficeId: Int): Single<List<DcReturnHandleBarcodeEntity>>

    fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>>

    fun findDcUnloadedBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun findDcReturnBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun findDcReturnBoxes(currentOfficeId: Int): Single<List<FlightBoxEntity>>

    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    fun removeDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable

    fun dcUnloadingCongratulation(currentOfficeId: Int): Single<DcCongratulationEntity>

    //==============================================================================================

    fun deleteAll()

}