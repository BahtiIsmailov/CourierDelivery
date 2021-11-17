package ru.wb.go.db

import ru.wb.go.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import ru.wb.go.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import ru.wb.go.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import ru.wb.go.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.go.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.go.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import ru.wb.go.db.entity.deliveryerrorbox.DeliveryUnloadingErrorBoxEntity
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.db.entity.flight.FlightEntity
import ru.wb.go.db.entity.flight.FlightOfficeEntity
import ru.wb.go.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import ru.wb.go.db.entity.unload.UnloadingTookAndPickupCountEntity
import ru.wb.go.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity
import ru.wb.go.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import ru.wb.go.ui.dcunloading.domain.DcUnloadingCounterEntity
import ru.wb.go.ui.splash.domain.AppDeliveryResult
import ru.wb.go.ui.unloadingcongratulation.domain.DeliveryResult
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppLocalRepository {

    fun saveFlightAndOffices(
        flightEntity: FlightEntity, flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable

    fun findFlightOffice(id: Int): Single<FlightOfficeEntity>

    fun updateFlightOfficeVisited(visitedAt: String, id: Int): Completable

    fun deleteFlightOffices()

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

    fun deleteFlightBoxesByBarcode(barcodes: List<String>): Completable

    fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun observeUnloadedAndUnloadOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<UnloadingUnloadedAndUnloadCountEntity>

    fun observeTookAndPickupOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<UnloadingTookAndPickupCountEntity>

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

    fun deletePvzMatchingBox(pvzMatchingBoxEntity: PvzMatchingBoxEntity): Completable

    fun deleteAllPvzMatchingBox()

    //==============================================================================================
    //TakeOnFlightBox
    //=============================================================================================

    fun findTakeOnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    fun observeTakeOnFlightBoxesByOfficeId(): Flowable<List<FlightBoxEntity>>

    fun observeTakeOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    fun readAllTakeOnFlightBox(): Single<List<FlightBoxEntity>>

    //==============================================================================================

    fun groupFlightPickupPointBoxGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>>

    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

    fun getNotDelivered(): Single<Int>

    fun getCongratulationDelivered(): Single<DeliveryResult>

    fun observeNavDrawerCountBoxes(): Flowable<AppDeliveryResult>

    //==============================================================================================
    //DcUnloadedBox
    //==============================================================================================

    fun saveDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable

    fun findDcReturnHandleBoxes(currentOfficeId: Int): Single<List<DcReturnHandleBarcodeEntity>>

    fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>>

    fun findDcUnloadedBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun findDcReturnBox(barcode: String, currentOfficeId: Int): Single<Optional<FlightBoxEntity>>

    fun findDcReturnBoxes(currentOfficeId: Int): Single<List<FlightBoxEntity>>

    @Deprecated("")
    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    fun observeDcUnloadingBarcodeBox(currentOfficeId: Int): Flowable<String>

    fun removeDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable

    //==============================================================================================

    fun dcUnloadedBoxes(): Single<Int>

    //==============================================================================================

    fun insertDeliveryErrorBoxEntity(deliveryErrorBoxEntity: DeliveryErrorBoxEntity): Completable

    fun insertNotUnloadingBoxToDeliveryErrorByOfficeId(currentOfficeId: Int): Completable

    @Deprecated("")
    fun changeNotUnloadingBoxToFlightBoxesByOfficeId(
        currentOfficeId: Int,
        updatedAt: String,
        onBoard: Boolean,
        status: Int,
    ): Completable

    fun deleteDeliveryErrorBoxByBarcode(barcode: String): Completable

    fun observeDeliveryUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<DeliveryUnloadingErrorBoxEntity>>

    fun observeDeliveryReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    //==============================================================================================

    fun observeDcUnloadingCounter(): Flowable<DcUnloadingCounterEntity>

    //==============================================================================================

    fun clearAll()

}