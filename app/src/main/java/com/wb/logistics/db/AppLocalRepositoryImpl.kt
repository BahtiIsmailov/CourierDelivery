package com.wb.logistics.db

import com.wb.logistics.db.dao.*
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.unload.UnloadingTookAndPickupCountEntity
import com.wb.logistics.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class AppLocalRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val flightDao: FlightDao,
    private val flightBoxDao: FlightBoxDao,
    private val warehouseMatchingBoxDao: WarehouseMatchingBoxDao,
    private val pvzMatchingBoxDao: PvzMatchingBoxDao,
    private val deliveryErrorBoxDao: DeliveryErrorBoxDao,
) : AppLocalRepository {

    override fun saveFlightAndOffices(
        flightEntity: FlightEntity,
        flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable {
        return flightDao.insertFlight(flightEntity)
            .andThen(flightDao.insertFlightOffice(flightOfficesEntity))
    }

    override fun findFlightOffice(id: Int): Single<FlightOfficeEntity> {
        return flightDao.findFlightOffice(id)
    }

    override fun updateFlightOfficeVisited(visitedAt: String, id: Int): Completable {
        return flightDao.updateFlightOfficeVisited(visitedAt, id)
    }

    override fun deleteFlightOffices() {
        return flightDao.deleteAllFlightOffices()
    }

    override fun observeFlightDataOptional(): Flowable<Optional<FlightData>> {
        return flightDao.observeFlightData().map { convertFlight(it) }
    }

    override fun readFlightOptional(): Single<Optional<FlightEntity>> {
        return flightDao.readFlight()
            .map<Optional<FlightEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun readFlight(): Single<FlightEntity> {
        return flightDao.readFlight()
    }

    override fun readFlightId(): Single<String> {
        return flightDao.readFlight().map { it.id.toString() }
    }

    private fun convertFlight(flightData: FlightDataEntity?): Optional<FlightData> {
        return if (flightData == null) {
            Optional.Empty()
        } else {
            successFlightData(flightData)
        }
    }

    // TODO: 09.04.2021 вынести в конвертер
    private fun successFlightData(flightDataEntity: FlightDataEntity): Optional<FlightData> {
        return with(flightDataEntity) {
            val addressesName = mutableListOf<String>()
            officeEntity.forEach { addresses -> addressesName.add(addresses.name) }
            Optional.Success(
                with(flightEntity) {
                    FlightData(
                        id,
                        gate,
                        plannedDate,
                        dc.name,
                        addressesName
                    )
                }
            )
        }
    }

    override fun deleteAllFlight() {
        flightDao.deleteAllFlight()
    }

    //==============================================================================================
    //scanned box
    //==============================================================================================

    override fun saveFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable {
        return flightBoxDao.insertFlightBoxes(flightBoxesEntity)
    }

    override fun saveFlightBox(flightBoxEntity: FlightBoxEntity): Completable {
        return flightBoxDao.insertFlightBox(flightBoxEntity)
    }

    override fun findFlightBox(barcode: String): Single<Optional<FlightBoxEntity>> {
        return flightBoxDao.findFlightBox(barcode)
            .map<Optional<FlightBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun readAllTakeOnFlightBox(): Single<List<FlightBoxEntity>> {
        return flightBoxDao.readAllFlightBox()
    }

    override fun findTakeOnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>> {
        return flightBoxDao.loadBox(barcodes)
    }

    override fun observeTakeOnFlightBoxesByOfficeId(): Flowable<List<FlightBoxEntity>> {
        return flightBoxDao.observeAttachedBox()
    }

    override fun observeTakeOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>> {
        return flightBoxDao.observeFilterByOfficeIdAttachedBoxes(currentOfficeId)
    }

    override fun deleteAllFlightBoxes() {
        return flightBoxDao.deleteAllFlightBox()
    }

    override fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable {
        return flightBoxDao.deleteFlightBoxes(flightBoxesEntity)
    }

    override fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>> {
        return flightBoxDao.observeUnloadingUnloadedBoxesByOfficeId(currentOfficeId)
    }

    override fun observeUnloadedAndUnloadOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<UnloadingUnloadedAndUnloadCountEntity> {
        return flightBoxDao.observeUnloadingUnloadedAndUnloadBoxes(currentOfficeId)
    }

    override fun observeTookAndPickupOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<UnloadingTookAndPickupCountEntity> {
        return flightBoxDao.observeUnloadingTookAndPickupBoxesByOfficeId(currentOfficeId)
    }

    override fun observeReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>> {
        return flightBoxDao.observeUnloadingReturnedBoxesByOfficeId(currentOfficeId)
    }

    override fun findReturnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>> {
        return flightBoxDao.findReturnedFlightBoxes(barcodes)
    }

    //==============================================================================================
    override fun saveWarehouseMatchingBoxes(warehouseMatchingBoxes: List<WarehouseMatchingBoxEntity>): Completable {
        return warehouseMatchingBoxDao.insertMatchingBoxes(warehouseMatchingBoxes)
    }

    override fun deleteWarehouseByBarcode(barcode: String): Completable {
        return warehouseMatchingBoxDao.deleteByBarcode(barcode)
    }

    override fun deleteAllWarehouseMatchingBox() {
        warehouseMatchingBoxDao.deleteAllMatchingBox()
    }

    //==============================================================================================
    override fun savePvzMatchingBoxes(pvzMatchingBoxes: List<PvzMatchingBoxEntity>): Completable {
        return pvzMatchingBoxDao.insertBoxes(pvzMatchingBoxes)
    }

    override fun readPvzMatchingBoxes(): Single<List<PvzMatchingBoxEntity>> {
        return pvzMatchingBoxDao.readBoxes()
    }

    override fun findPvzMatchingBox(barcode: String): Single<Optional<PvzMatchingBoxEntity>> {
        return pvzMatchingBoxDao.findBox(barcode)
            .map<Optional<PvzMatchingBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun observePvzMatchingBoxByOfficeId(currentOfficeId: Int): Flowable<List<PvzMatchingBoxEntity>> {
        return pvzMatchingBoxDao.observePvzMatchingBoxByOfficeId(currentOfficeId)
    }

    override fun deletePvzMatchingBox(pvzMatchingBoxEntity: PvzMatchingBoxEntity): Completable {
        return pvzMatchingBoxDao.deletePvzMatchingBox(pvzMatchingBoxEntity)
    }

    override fun deleteAllPvzMatchingBox() {
        pvzMatchingBoxDao.deleteAllBox()
    }

    //==============================================================================================
    //dcunloaded boxes
    //==============================================================================================

    override fun saveDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable {
        return flightBoxDao.insertFlightBox(flightBoxEntity)
    }

    override fun findDcReturnHandleBoxes(currentOfficeId: Int): Single<List<DcReturnHandleBarcodeEntity>> {
        return flightBoxDao.findDcReturnHandleBoxes()
    }

    override fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>> {
        return flightBoxDao.findDcUnloadedBarcodes()
    }

    override fun findDcUnloadedBox(
        barcode: String,
        currentOfficeId: Int,
    ): Single<Optional<FlightBoxEntity>> {
        return flightBoxDao.findDcUnloadedBox(barcode, currentOfficeId)
            .map<Optional<FlightBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun findDcReturnBox(
        barcode: String,
        currentOfficeId: Int,
    ): Single<Optional<FlightBoxEntity>> {
        return flightBoxDao.findDcReturnBox(barcode, currentOfficeId)
            .map<Optional<FlightBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun findDcReturnBoxes(currentOfficeId: Int): Single<List<FlightBoxEntity>> {
        return flightBoxDao.findDcReturnBoxes()
    }

    override fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity> {
        return flightBoxDao.observeDcUnloadingScanBox(currentOfficeId)
    }

    override fun observeDcUnloadingBarcodeBox(currentOfficeId: Int): Flowable<String> {
        return flightBoxDao.observeDcUnloadingBarcodeBox()
    }

    override fun removeDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable {
        return flightBoxDao.deleteFlightBox(flightBoxEntity)
    }

    override fun groupFlightPickupPointBoxGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>> {
        return flightBoxDao.groupFlightPickupPointBoxGroupByOffice()
    }

    override fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>> {
        return flightBoxDao.groupDeliveryBoxByOffice()
    }

    override fun deleteAll() {
        appDatabase.clearAllTables()
    }

    //==============================================================================================
    override fun insertDeliveryErrorBoxEntity(deliveryErrorBoxEntity: DeliveryErrorBoxEntity): Completable {
        return deliveryErrorBoxDao.insert(deliveryErrorBoxEntity)
    }

    override fun findDeliveryErrorBoxByOfficeId(currentOfficeId: Int): Single<List<DeliveryErrorBoxEntity>> {
        return deliveryErrorBoxDao.findDeliveryErrorBoxByOfficeId(currentOfficeId)
    }

}