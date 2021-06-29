package com.wb.logistics.db

import com.wb.logistics.db.dao.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class AppLocalRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val flightDao: FlightDao,
    private val attachedBoxDao: AttachedBoxDao,
    private val flightBoxDao: FlightBoxDao,
    private val warehouseMatchingBoxDao: WarehouseMatchingBoxDao,
    private val pvzMatchingBoxDao: PvzMatchingBoxDao,
) : AppLocalRepository {

    override fun saveFlightAndOffices(
        flightEntity: FlightEntity,
        flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable {
        return flightDao.insertFlight(flightEntity)
            .andThen(flightDao.insertFlightOffice(flightOfficesEntity))
    }

    override fun findFlightOfficeOptional(id: Int): Single<Optional<FlightOfficeEntity>> {
        return flightDao.findFlightOffice(id)
            .map<Optional<FlightOfficeEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun findFlightOffice(id: Int): Single<FlightOfficeEntity> {
        return flightDao.findFlightOffice(id)
    }

    override fun observeFlightDataOptional(): Flowable<Optional<FlightData>> {
        return flightDao.observeFlightData().map { convertFlight(it) }
    }

    override fun observeFlightData(): Flowable<FlightData> {
        return flightDao.observeFlightData().map { convertFlightData(it) }
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

    override fun readFlightDataOptional(): Single<Optional<FlightData>> {
        return flightDao.readFlightData().map { convertFlight(it) }
    }

    private fun convertFlight(flightData: FlightDataEntity?): Optional<FlightData> {
        return if (flightData == null) {
            Optional.Empty()
        } else {
            successFlightData(flightData)
        }
    }

    private fun convertFlightData(flightDataEntity: FlightDataEntity): FlightData {
        return with(flightDataEntity) {
            val addressesName = mutableListOf<String>()
            officeEntity.forEach { addresses -> addressesName.add(addresses.name) }
            with(flightEntity) {
                FlightData(
                    id,
                    gate,
                    plannedDate,
                    dc.name,
                    addressesName
                )
            }
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

    override fun deleteAllFlightBoxes() {
        return flightBoxDao.deleteAllFlightBox()
    }

    override fun deleteReturnFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable {
        return flightBoxDao.deleteReturnFlightBoxes(flightBoxesEntity)
    }

    override fun findUnloadedFlightBox(
        barcode: String,
        currentOfficeId: Int,
    ): Single<Optional<FlightBoxEntity>> {
        return flightBoxDao.findUnloadedFlightBox(barcode, currentOfficeId)
            .map<Optional<FlightBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>> {
        return flightBoxDao.observeUnloadedFlightBoxesByOfficeId(currentOfficeId)
    }

    override fun findReturnedFlightBox(
        barcode: String,
        currentOfficeId: Int,
    ): Single<Optional<FlightBoxEntity>> {
        return flightBoxDao.findReturnedFlightBox(barcode, currentOfficeId)
            .map<Optional<FlightBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun observeReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>> {
        return flightBoxDao.observeReturnedFlightBoxesByOfficeId(currentOfficeId)
    }

    override fun findReturnFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>> {
        return flightBoxDao.findReturnedFlightBoxes(barcodes)
    }

    //==============================================================================================
    override fun saveWarehouseMatchingBoxes(warehouseMatchingBoxes: List<WarehouseMatchingBoxEntity>): Completable {
        return warehouseMatchingBoxDao.insertMatchingBoxes(warehouseMatchingBoxes)
    }

    override fun saveWarehouseMatchingBox(warehouseMatchingBox: WarehouseMatchingBoxEntity): Completable {
        return warehouseMatchingBoxDao.insertMatchingBox(warehouseMatchingBox)
    }

    override fun deleteWarehouseMatchingBox(warehouseMatchingBox: WarehouseMatchingBoxEntity): Completable {
        return warehouseMatchingBoxDao.deleteMatchingBox(warehouseMatchingBox)
    }

    override fun findWarehouseMatchingBox(barcode: String): Single<Optional<WarehouseMatchingBoxEntity>> {
        return warehouseMatchingBoxDao.findMatchingBox(barcode)
            .map<Optional<WarehouseMatchingBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun deleteAllWarehouseMatchingBox() {
        warehouseMatchingBoxDao.deleteAllMatchingBox()
    }

    //==============================================================================================
    override fun savePvzMatchingBoxes(pvzMatchingBoxes: List<PvzMatchingBoxEntity>): Completable {
        return pvzMatchingBoxDao.insertBoxes(pvzMatchingBoxes)
    }

    override fun savePvzMatchingBox(pvzMatchingBox: PvzMatchingBoxEntity): Completable {
        return pvzMatchingBoxDao.insertBox(pvzMatchingBox)
    }

    override fun readPvzMatchingBoxes(): Single<List<PvzMatchingBoxEntity>> {
        return pvzMatchingBoxDao.readBoxes()
    }

    override fun deletePvzMatchingBox(pvzMatchingBox: PvzMatchingBoxEntity): Completable {
        return pvzMatchingBoxDao.deleteBox(pvzMatchingBox)
    }

    override fun findPvzMatchingBox(barcode: String): Single<Optional<PvzMatchingBoxEntity>> {
        return pvzMatchingBoxDao.findBox(barcode)
            .map<Optional<PvzMatchingBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun deleteAllPvzMatchingBox() {
        pvzMatchingBoxDao.deleteAllBox()
    }

    //==============================================================================================
    //scanned box
    //==============================================================================================
    override fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable {
        return attachedBoxDao.insertAttachedBox(attachedBoxEntity)
    }

    override fun saveAttachedBoxes(attachedBoxesEntity: List<AttachedBoxEntity>): Completable {
        return attachedBoxDao.insertAttachedBoxes(attachedBoxesEntity)
    }

    override fun findAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>> {
        return attachedBoxDao.loadAttachedBox(barcodes)
    }

    override fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>> {
        return attachedBoxDao.observeAttachedBox()
    }

    override fun observeAttachedBoxes(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>> {
        return attachedBoxDao.observeFilterByOfficeIdAttachedBoxes(dstOfficeId)
    }

    override fun readAttachedBoxes(): Single<List<AttachedBoxEntity>> {
        return attachedBoxDao.readAttachedBox()
    }

    override fun findAttachedBox(barcode: String): Single<Optional<AttachedBoxEntity>> {
        return attachedBoxDao.findAttachedBox(barcode)
            .map<Optional<AttachedBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable {
        return attachedBoxDao.deleteAttachedBox(attachedBoxEntity)
    }

    override fun deleteAttachedBoxes(attachedBoxesEntity: List<AttachedBoxEntity>): Completable {
        return attachedBoxDao.deleteAttachedBoxes(attachedBoxesEntity)
    }

    override fun deleteAllAttachedBox() {
        attachedBoxDao.deleteAllAttachedBox()
    }

    //==============================================================================================
    //dcunloaded boxes
    //==============================================================================================

    override fun saveDcUnloadedBox(dcUnloadedBoxEntity: FlightBoxEntity): Completable {
        return flightBoxDao.insertFlightBox(dcUnloadedBoxEntity)
    }

    override fun saveDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable {
        return flightBoxDao.insertFlightBox(flightBoxEntity)
    }

    override fun findDcReturnHandleBoxes(currentOfficeId: Int): Single<List<DcReturnHandleBarcodeEntity>> {
        return flightBoxDao.findDcReturnHandleBoxes(currentOfficeId)
    }

    override fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>> {
        return flightBoxDao.findDcUnloadedBarcodes(currentOfficeId)
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
        return flightBoxDao.findDcReturnBoxes(currentOfficeId)
    }

    override fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity> {
        return flightBoxDao.observeDcUnloadingScanBox(currentOfficeId)
    }

    override fun removeDcUnloadedReturnBox(flightBoxEntity: FlightBoxEntity): Completable {
        return flightBoxDao.deleteReturnFlightBox(flightBoxEntity)
    }

    override fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>> {
        return attachedBoxDao.groupDeliveryBoxByOffice()
    }

    override fun dcUnloadingCongratulation(currentOfficeId: Int): Single<DcCongratulationEntity> {
        return flightBoxDao.dcUnloadingCongratulation(currentOfficeId)
    }

    override fun deleteAll() {
        appDatabase.clearAllTables()
    }

}