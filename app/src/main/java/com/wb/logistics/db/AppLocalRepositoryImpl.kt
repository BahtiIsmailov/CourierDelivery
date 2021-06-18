package com.wb.logistics.db

import com.wb.logistics.db.dao.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.dcunloadedboxes.*
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
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

class AppLocalRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val flightDao: FlightDao,
    private val attachedBoxDao: AttachedBoxDao,
    private val unloadingBoxDao: UnloadingBoxDao,
    private val returnBoxDao: ReturnBoxDao,
    private val dcUnloadingBoxDao: DcUnloadingBoxDao,
    private val flightMatchingDao: FlightBoxDao,
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

    override fun changeFlightOfficeUnloading(
        dstOfficeId: Int,
        isUnloading: Boolean,
        notUnloadingCause: String,
    ): Completable {
        return flightDao.changeFlightOfficeUnloading(dstOfficeId, isUnloading, notUnloadingCause)
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

    override fun saveFlightBoxes(flightMatchingBoxes: List<FlightBoxEntity>): Completable {
        return flightMatchingDao.insertFlightBoxes(flightMatchingBoxes)
    }

    override fun findFlightBox(barcode: String): Single<Optional<FlightBoxEntity>> {
        return flightMatchingDao.findFlightBox(barcode)
            .map<Optional<FlightBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun deleteAllFlightBoxes() {
        return flightMatchingDao.deleteAllFlightBox()
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

    override fun groupAttachedBoxByDstAddress(): Single<List<AttachedBoxGroupByOfficeEntity>> {
        return attachedBoxDao.groupAttachedBoxByDstAddress()
    }

    override fun groupAttachedBox(): Single<AttachedBoxResultEntity> {
        return attachedBoxDao.groupAttachedBox()
    }

    //==============================================================================================
    //unloaded boxes
    //==============================================================================================

    override fun saveUnloadedBox(unloadedBoxEntity: UnloadedBoxEntity): Completable {
        return unloadingBoxDao.insertUnloadingBox(unloadedBoxEntity)
    }

    override fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>> {
        return unloadingBoxDao.observeUnloadingBox()
    }

    override fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>> {
        return unloadingBoxDao.observeFilterByOfficeIdAttachedBoxes(dstOfficeId)
    }

    override fun findUnloadedBox(barcode: String): Single<Optional<UnloadedBoxEntity>> {
        return unloadingBoxDao.findUnloadedBox(barcode)
            .map<Optional<UnloadedBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun deleteAllUnloadedBox() {
        TODO("Not yet implemented")
    }

    //==============================================================================================
    //dcunloaded boxes
    //==============================================================================================
    override fun saveDcUnloadedBox(dcUnloadedBoxEntity: DcUnloadedBoxEntity): Completable {
        return dcUnloadingBoxDao.insertDcUnloadingBox(dcUnloadedBoxEntity)
    }

    override fun saveDcUnloadedReturnBox(dcUnloadedReturnBoxEntity: DcUnloadedReturnBoxEntity): Completable {
        return dcUnloadingBoxDao.insertDcUnloadingReturnBox(dcUnloadedReturnBoxEntity)
    }

    override fun findDcUnloadedBox(barcode: String): Single<Optional<DcUnloadedBoxEntity>> {
        return dcUnloadingBoxDao.findDcUnloadedBox(barcode)
            .map<Optional<DcUnloadedBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>> {
        return dcUnloadingBoxDao.findDcUnloadedHandleBoxes()
    }

    override fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>> {
        return dcUnloadingBoxDao.findDcUnloadedListBoxes()
    }

    override fun observeDcUnloadingScanBox(): Flowable<DcUnloadingScanBoxEntity> {
        return dcUnloadingBoxDao.observeDcUnloadingScanBox()
    }

    override fun congratulation(): Single<DcCongratulationEntity> {
        return dcUnloadingBoxDao.congratulation()
    }

    override fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>> {
        return dcUnloadingBoxDao.notDcUnloadedBoxes()
    }

    override fun deleteAllDcUnloadedBox() {
        TODO("Not yet implemented")
    }

    //==============================================================================================
    //return box
    //==============================================================================================

    override fun saveReturnBox(returnBoxEntity: ReturnBoxEntity): Completable {
        return returnBoxDao.insertReturnBoxEntity(returnBoxEntity)
    }

    override fun observedReturnBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>> {
        return returnBoxDao.observeFilterByOfficeIdReturnBoxes(dstOfficeId)
    }

    override fun findReturnBox(barcode: String): Single<Optional<ReturnBoxEntity>> {
        return returnBoxDao.findReturnBox(barcode)
            .map<Optional<ReturnBoxEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    override fun findReturnBoxes(barcodes: List<String>): Single<List<ReturnBoxEntity>> {
        return returnBoxDao.findReturnBoxes(barcodes)
    }

    override fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity): Completable {
        return returnBoxDao.deleteReturnBox(returnBoxEntity)
    }

    override fun groupByDstAddressReturnBox(dstOfficeId: Int): Single<List<ReturnBoxByAddressEntity>> {
        return returnBoxDao.groupByDstAddressReturnBox(dstOfficeId)
    }

    override fun deleteAllReturnBox() {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        appDatabase.clearAllTables()
    }

}