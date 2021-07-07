package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.deliveryboxes.FlightPickupPointBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.unload.UnloadingTookAndPickupCountEntity
import com.wb.logistics.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity
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

    // TODO: 07.07.2021 вынести в отдельный DAO

    @Query("SELECT COUNT(*) AS unloadedCount, barcode AS barcode, updatedAt as updatedAt, (SELECT COUNT(*) FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3) + COUNT(*) AS unloadCount FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 0 AND status = 5 ORDER BY updatedAt DESC LIMIT 1")
    fun observeUnloadingUnloadedAndUnloadBoxes(currentOfficeId: Int): Flowable<UnloadingUnloadedAndUnloadCountEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 0 ORDER BY updatedAt")
    fun observeUnloadingUnloadedBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT COUNT(*) AS tookCount, barcode AS barcode, updatedAt as updatedAt, (SELECT COUNT(*) FROM PvzMatchingBoxEntity WHERE src_office_id = :currentOfficeId) + COUNT(*) AS pickupCount FROM FlightBoxEntity WHERE src_office_id = :currentOfficeId AND onBoard = 1 ORDER BY updatedAt DESC LIMIT 1")
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

    @Query("SELECT barcode AS barcode, updatedAt AS updatedAt FROM FlightBoxEntity WHERE onBoard = 0 AND status = 6")
    fun findDcUnloadedBarcodes(): Single<List<DcUnloadingBarcodeEntity>>

    @Delete
    fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

//    @Query("delete from FlightBoxEntity where barcode in (:barcodes)")
//    fun deleteFlightBoxesByBarcodes(barcodes: List<String>): Completable

    @Query("SELECT COUNT(*) AS dcUnloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE onBoard = 1) AS dcReturnCount FROM FlightBoxEntity WHERE ((dst_office_id = :currentOfficeId OR dst_office_id <= 0) AND onBoard = 0) OR ((src_office_id = :currentOfficeId OR src_office_id <= 0) AND onBoard = 0 AND status = 6)")
    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    @Query("SELECT barcode FROM FlightBoxEntity WHERE onBoard = 0 AND status = 6 ORDER BY updatedAt DESC LIMIT 1")
    fun observeDcUnloadingBarcodeBox(): Flowable<String>

    @Query("SELECT * FROM FlightBoxEntity WHERE onBoard = 1")
    fun findDcReturnBoxes(): Single<List<FlightBoxEntity>>

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.dst_office_id AND onBoard = 1) AS deliverCount, (SELECT COUNT(*) FROM PvzMatchingBoxEntity WHERE FlightOfficeEntity.office_id = PvzMatchingBoxEntity.src_office_id) AS pickUpCount FROM FlightOfficeEntity")
    fun groupFlightPickupPointBoxGroupByOffice(): Single<List<FlightPickupPointBoxGroupByOfficeEntity>>

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.dst_office_id AND FlightBoxEntity.onBoard = 1) AS deliverCount, (SELECT COUNT(*) FROM PvzMatchingBoxEntity WHERE FlightOfficeEntity.office_id = PvzMatchingBoxEntity.src_office_id) AS returnCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.dst_office_id AND FlightBoxEntity.onBoard = 0 AND status = 5) AS deliveredCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.src_office_id AND FlightBoxEntity.onBoard = 1) AS returnedCount FROM FlightOfficeEntity")
    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun loadBox(barcodes: List<String>): Single<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity ORDER BY updatedAt")
    fun observeAttachedBox(): Flowable<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3")
    fun observeFilterByOfficeIdAttachedBoxes(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

}