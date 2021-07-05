package com.wb.logistics.db

import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightUnloadedAndUnloadCountEntity
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

    fun findFlightOffice(id: Int): Single<FlightOfficeEntity>

    fun observeFlightDataOptional(): Flowable<Optional<FlightData>>

    fun readFlightOptional(): Single<Optional<FlightEntity>>

    fun readFlight(): Single<FlightEntity>

    fun readFlightId(): Single<String>

    fun deleteAllFlight()

    //==============================================================================================
    //flight boxes
    //==============================================================================================
    fun saveFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    fun saveFlightBox(flightBoxEntity: FlightBoxEntity): Completable

    fun findFlightBox(barcode: String): Single<Optional<FlightBoxEntity>>

    fun deleteAllFlightBoxes()

    fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun observeUnloadedAndTakeOnFlightBoxes(currentOfficeId: Int): Flowable<FlightUnloadedAndUnloadCountEntity>

    fun observeReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun findReturnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    //==============================================================================================
    //warehouse matching boxes
    //==============================================================================================
    fun saveWarehouseMatchingBoxes(warehouseMatchingBoxes: List<WarehouseMatchingBoxEntity>): Completable

    fun deleteWarehouseByBarcode(barcode: String): Completable

    fun deleteAllWarehouseMatchingBox()

    //==============================================================================================
    //pvz matching boxes
    //==============================================================================================
    fun savePvzMatchingBoxes(pvzMatchingBoxes: List<PvzMatchingBoxEntity>): Completable

    fun readPvzMatchingBoxes(): Single<List<PvzMatchingBoxEntity>>

    fun findPvzMatchingBox(barcode: String): Single<Optional<PvzMatchingBoxEntity>>

    fun observePvzMatchingBoxByOfficeId(currentOfficeId: Int): Flowable<List<PvzMatchingBoxEntity>>

    fun deleteAllPvzMatchingBox()

    //==============================================================================================
    //TakeOnFlightBox
    //=============================================================================================

    fun findTakeOnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    fun observeTakeOnFlightBoxesByOfficeId(): Flowable<List<FlightBoxEntity>>

    fun observeTakeOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun readAllTakeOnFlightBox(): Single<List<FlightBoxEntity>>

    //==============================================================================================

    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

    //==============================================================================================
    //DcUnloadedBox
    //==============================================================================================

    fun saveDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable

    fun findDcReturnHandleBoxes(currentOfficeId: Int): Single<List<DcReturnHandleBarcodeEntity>>

    fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>>

    fun findDcUnloadedBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun findDcReturnBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun findDcReturnBoxes(currentOfficeId: Int): Single<List<FlightBoxEntity>>

    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    fun observeDcUnloadingBarcodeBox(currentOfficeId: Int): Flowable<String>

    fun removeDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable

    //==============================================================================================

    fun insertDeliveryErrorBoxEntity(deliveryErrorBoxEntity: DeliveryErrorBoxEntity): Completable

    fun findDeliveryErrorBoxByOfficeId(currentOfficeId: Int): Single<List<DeliveryErrorBoxEntity>>

    //==============================================================================================

    fun deleteAll()

}