package com.wb.logistics.db

import com.wb.logistics.db.dao.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.dcunloadedboxes.*
import com.wb.logistics.db.entity.flight.FlightDataEntity
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

class AppLocalRepositoryImpl(
    private val flightDao: FlightDao,
    private val attachedBoxDao: AttachedBoxDao,
    private val unloadingBox: UnloadingBoxDao,
    private val returnBoxDao: ReturnBoxDao,
    private val dcUnloadingBox: DcUnloadingBoxDao,
) : AppLocalRepository {

    override fun saveFlight(
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

    override fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return flightDao.observeFlight().map { convertFlight(it) }
    }

    override fun readFlight(): Single<SuccessOrEmptyData<FlightEntity>> {
        return flightDao.readFlight()
            .map<SuccessOrEmptyData<FlightEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun readFlightData(): Single<SuccessOrEmptyData<FlightData>> {
        return flightDao.readFlightData().map { convertFlight(it) }
    }

    private fun convertFlight(flightData: FlightDataEntity?): SuccessOrEmptyData<FlightData> {
        return if (flightData == null) {
            SuccessOrEmptyData.Empty()
        } else {
            successFlightData(flightData)
        }
    }

    // TODO: 09.04.2021 вынести в конертер
    private fun successFlightData(flightDataEntity: FlightDataEntity): SuccessOrEmptyData<FlightData> {
        return with(flightDataEntity) {
            val addressesName = mutableListOf<String>()
            officeEntity.forEach { addresses -> addressesName.add(addresses.name) }
            SuccessOrEmptyData.Success(
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

    override fun saveFlightBoxes(boxesEntity: List<FlightBoxEntity>): Completable {
        return flightDao.insertFlightBoxes(boxesEntity)
    }

    override fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>> {
        return flightDao.findFlightBox(barcode)
            .map<SuccessOrEmptyData<FlightBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteAllFlightBoxes() {
        flightDao.deleteAllFlightBoxes()
    }

    //==============================================================================================
    override fun saveMatchingBoxes(matchingBoxes: List<MatchingBoxEntity>): Completable {
        return flightDao.insertMatchingBoxes(matchingBoxes)
    }

    override fun findMatchingBox(barcode: String): Single<SuccessOrEmptyData<MatchingBoxEntity>> {
        return flightDao.findMatchingBox(barcode)
            .map<SuccessOrEmptyData<MatchingBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteAllMatchingBox() {
        flightDao.deleteAllMatchingBox()
    }

    //==============================================================================================
    //scanned box
    //==============================================================================================
    override fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable {
        return attachedBoxDao.insertScannedBox(attachedBoxEntity)
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

    override fun findAttachedBox(barcode: String): Single<SuccessOrEmptyData<AttachedBoxEntity>> {
        return attachedBoxDao.findAttachedBox(barcode)
            .map<SuccessOrEmptyData<AttachedBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable {
        return attachedBoxDao.deleteAttachedBox(attachedBoxEntity)
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
        return unloadingBox.insertUnloadingBox(unloadedBoxEntity)
    }

    override fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>> {
        return unloadingBox.observeUnloadingBox()
    }

    override fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>> {
        return unloadingBox.observeFilterByOfficeIdAttachedBoxes(dstOfficeId)
    }

    override fun findUnloadedBox(barcode: String): Single<SuccessOrEmptyData<UnloadedBoxEntity>> {
        return unloadingBox.findUnloadedBox(barcode)
            .map<SuccessOrEmptyData<UnloadedBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }


    //==============================================================================================
    //dcunloaded boxes
    //==============================================================================================
    override fun saveDcUnloadedBox(dcUnloadedBoxEntity: DcUnloadedBoxEntity): Completable {
        return dcUnloadingBox.insertDcUnloadingBox(dcUnloadedBoxEntity)
    }

    override fun saveDcUnloadedReturnBox(dcUnloadedReturnBoxEntity: DcUnloadedReturnBoxEntity): Completable {
        return dcUnloadingBox.insertDcUnloadingReturnBox(dcUnloadedReturnBoxEntity)
    }

    override fun findDcUnloadedBox(barcode: String): Single<SuccessOrEmptyData<DcUnloadedBoxEntity>> {
        return dcUnloadingBox.findDcUnloadedBox(barcode)
            .map<SuccessOrEmptyData<DcUnloadedBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>> {
        return dcUnloadingBox.findDcUnloadedHandleBoxes()
    }

    override fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>> {
        return dcUnloadingBox.findDcUnloadedListBoxes()
    }

    override fun observeDcUnloadingScanBox(): Flowable<DcUnloadingScanBoxEntity> {
        return dcUnloadingBox.observeDcUnloadingScanBox()
    }

    override fun congratulation(): Single<DcCongratulationEntity> {
        return dcUnloadingBox.congratulation()
    }

    override fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>> {
        return dcUnloadingBox.notDcUnloadedBoxes()
    }

    //==============================================================================================
    //balance box
    //==============================================================================================

    override fun saveFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable {
        return flightDao.insertFlightBoxBalanceAwait(flightBoxBalanceEntity)
    }

    override fun observeFlightBoxBalanceAwait(): Flowable<List<AttachedBoxBalanceAwaitEntity>> {
        return flightDao.observeFlightBoxBalanceAwait()
    }

    override fun flightBoxBalanceAwait(): Single<List<AttachedBoxBalanceAwaitEntity>> {
        return flightDao.flightBoxBalanceAwait()
    }

    override fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable {
        return flightDao.deleteFlightBoxBalanceAwait(flightBoxBalanceEntity)
    }

    override fun deleteAllFlightBoxBalanceAwait() {
        flightDao.deleteAllFlightBoxBalanceAwait()
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

    override fun findReturnBox(barcode: String): Single<SuccessOrEmptyData<ReturnBoxEntity>> {
        return returnBoxDao.findReturnBox(barcode)
            .map<SuccessOrEmptyData<ReturnBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
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

}