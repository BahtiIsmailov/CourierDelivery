package ru.wb.perevozka.db.dao

import androidx.room.*
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import ru.wb.perevozka.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.perevozka.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.unload.UnloadingTookAndPickupCountEntity
import ru.wb.perevozka.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity
import ru.wb.perevozka.ui.dcunloading.domain.DcUnloadingCounterEntity
import ru.wb.perevozka.ui.splash.domain.AppDeliveryResult
import ru.wb.perevozka.ui.unloadingcongratulation.domain.DeliveryResult
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface FlightBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxes(flightBoxes: List<FlightBoxEntity>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBox(flightMatchingBox: FlightBoxEntity): Completable

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode")
    fun findFlightBox(barcode: String): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity")
    fun readAllFlightBox(): Single<List<FlightBoxEntity>>

    @Delete
    fun deleteFlightBox(flightBoxesEntity: FlightBoxEntity): Completable

    @Query("DELETE FROM FlightBoxEntity")
    fun deleteAllFlightBox()

    @Query("DELETE FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun deleteFlightBoxesByBarcode(barcodes: List<String>): Completable

    @Delete
    fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    // TODO: 07.07.2021 вынести в отдельный DAO

    @Query("SELECT COUNT(*) AS unloadedCount, barcode AS barcode, updatedAt as updatedAt, (SELECT COUNT(*) FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3) + COUNT(*) AS unloadCount FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 0 AND status = 5 ORDER BY updatedAt DESC LIMIT 1")
    fun observeUnloadingUnloadedAndUnloadBoxes(currentOfficeId: Int): Flowable<UnloadingUnloadedAndUnloadCountEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 0 ORDER BY updatedAt")
    fun observeUnloadingUnloadedBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT LastBarcode.barcode, LastBarcode.updatedAt, LastBarcode.tookCount, PvzMatching.pvzCount + LastBarcode.tookCount AS pickupCount  FROM  (SELECT COUNT(*) AS tookCount, barcode AS barcode, updatedAt as updatedAt FROM (SELECT barcode, updatedAt FROM FlightBoxEntity WHERE src_office_id = :currentOfficeId AND onBoard = 1 ORDER BY updatedAt)) as LastBarcode, (SELECT COUNT(*) AS pvzCount FROM PvzMatchingBoxEntity WHERE src_office_id = :currentOfficeId) AS PvzMatching")
    fun observeUnloadingTookAndPickupBoxesByOfficeId(currentOfficeId: Int): Flowable<UnloadingTookAndPickupCountEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE src_office_id = :currentOfficeId AND onBoard = 1 ORDER BY updatedAt")
    fun observeUnloadingReturnedBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>


    @Query("SELECT * FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun findReturnedFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND (dst_office_id = :currentOfficeId OR dst_office_id <= 0) AND onBoard = 0")
    fun findDcUnloadedBox(barcode: String, currentOfficeId: Int): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND (dst_office_id = :currentOfficeId OR dst_office_id <= 0) AND onBoard = 1")
    fun findDcReturnBox(barcode: String, currentOfficeId: Int): Single<FlightBoxEntity>

    @Query("SELECT barcode AS barcode FROM FlightBoxEntity WHERE onBoard = 1")
    fun findDcReturnHandleBoxes(): Single<List<DcReturnHandleBarcodeEntity>>

    @Query("SELECT barcode AS barcode, updatedAt AS updatedAt FROM FlightBoxEntity WHERE onBoard = 0 AND status = 6 ORDER BY updatedAt")
    fun findDcUnloadedBarcodes(): Single<List<DcUnloadingBarcodeEntity>>

    @Query("SELECT COUNT(*) AS dcUnloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE onBoard = 1) AS dcReturnCount FROM FlightBoxEntity WHERE ((dst_office_id = :currentOfficeId OR dst_office_id <= 0) AND onBoard = 0) OR ((src_office_id = :currentOfficeId OR src_office_id <= 0) AND onBoard = 0 AND status = 6)")
    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    @Query("SELECT barcode FROM FlightBoxEntity WHERE onBoard = 0 AND status = 6 ORDER BY updatedAt DESC LIMIT 1")
    fun observeDcUnloadingBarcodeBox(): Flowable<String>

    @Query("SELECT * FROM FlightBoxEntity WHERE onBoard = 1")
    fun findDcReturnBoxes(): Single<List<FlightBoxEntity>>

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.dst_office_id AND onBoard = 1) AS deliverCount, (SELECT COUNT(*) FROM PvzMatchingBoxEntity WHERE FlightOfficeEntity.office_id = PvzMatchingBoxEntity.src_office_id) AS pickUpCount FROM FlightOfficeEntity")
    fun groupFlightPickupPointBoxGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>>

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, visitedAt AS visitedAt, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.dst_office_id AND FlightBoxEntity.onBoard = 1) AS deliverCount, (SELECT COUNT(*) FROM PvzMatchingBoxEntity WHERE FlightOfficeEntity.office_id = PvzMatchingBoxEntity.src_office_id) AS returnCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.dst_office_id AND FlightBoxEntity.onBoard = 0 AND status = 5) AS deliveredCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.src_office_id AND FlightBoxEntity.onBoard = 1) AS returnedCount FROM FlightOfficeEntity")
    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

    @Query("SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightBoxEntity.src_office_id = (SELECT dc_id FROM FlightEntity) AND onBoard = 1")
    fun getNotDelivered(): Single<Int>

    @Query("SELECT (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightBoxEntity.src_office_id = dc_id AND onBoard = 0 AND status != 6) as unloadedCount, (SELECT COUNT(*) as attachedCount FROM FlightBoxEntity WHERE FlightBoxEntity.src_office_id = dc_id) as attachedCount FROM (SELECT dc_id FROM FlightEntity) as dc_id")
    fun getCongratulationDelivered(): Single<DeliveryResult>

    @Query("SELECT (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightBoxEntity.src_office_id = dc_id) as acceptedCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightBoxEntity.src_office_id != dc_id) as returnCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightBoxEntity.src_office_id = dc_id AND onBoard = 0) as deliveryCount, (SELECT COUNT(*) FROM DeliveryErrorBoxEntity) as debtCount FROM (SELECT dc_id FROM FlightEntity) as dc_id")
    fun getAppDelivered(): Flowable<AppDeliveryResult>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun loadBox(barcodes: List<String>): Single<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity ORDER BY updatedAt")
    fun observeAttachedBox(): Flowable<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3")
    fun observeTakeOnFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT COUNT(*) FROM FlightBoxEntity WHERE onBoard = 1")
    fun dcUnloadedBoxes(): Single<Int>

    @Query("SELECT (SELECT COUNT(*) FROM FlightBoxEntity WHERE ((dst_office_id = dc_id OR dst_office_id <= 0) AND onBoard = 0) OR (onBoard = 0 AND status = 6)) AS unloadedCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE onBoard = 1) AS leftUnload, (SELECT barcode FROM FlightBoxEntity WHERE status = 6 ORDER BY updatedAt DESC LIMIT 1) AS barcode FROM (SELECT dc_id FROM FlightEntity) as dc_id")
    fun observeDcUnloadingCounter(): Flowable<DcUnloadingCounterEntity>

}