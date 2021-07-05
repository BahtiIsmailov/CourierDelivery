package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightUnloadedAndUnloadCountEntity
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
    fun readAllBox(): Single<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND onBoard = 0 AND status = 5")
    fun findUnloadedFlightBox(barcode: String): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 0")
    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT COUNT(*) AS unloadCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 0 AND status = 5) AS unloadedCount, (SELECT barcode FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 0 AND status = 5 ORDER BY updatedAt DESC LIMIT 1) AS barcode FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3")
    fun observeUnloadedAndUnloadFlightBoxes(currentOfficeId: Int): Flowable<FlightUnloadedAndUnloadCountEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND onBoard = 1")
    fun findReturnedFlightBox(barcode: String): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE flight_src_office_id = :currentOfficeId AND onBoard = 1")
    fun observeReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun findReturnedFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0")
    fun findDcUnloadedBox(barcode: String, currentOfficeId: Int): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 1")
    fun findDcReturnBox(barcode: String, currentOfficeId: Int): Single<FlightBoxEntity>

    @Query("SELECT barcode AS barcode FROM FlightBoxEntity WHERE onBoard = 1")
    fun findDcReturnHandleBoxes(): Single<List<DcReturnHandleBarcodeEntity>>

    @Query("SELECT barcode AS barcode, updatedAt AS updatedAt FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0")
    fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>>

    @Delete
    fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    @Query("delete from FlightBoxEntity where barcode in (:barcodes)")
    fun deleteFlightBoxesByBarcodes(barcodes: List<String>): Completable

    @Delete
    fun deleteFlightBox(flightBoxesEntity: FlightBoxEntity): Completable

    @Query("DELETE FROM FlightBoxEntity")
    fun deleteAllFlightBox()

    @Query("SELECT COUNT(*) AS dcUnloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE onBoard = 1) AS dcReturnCount FROM FlightBoxEntity WHERE ((flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0) OR ((flight_src_office_id = :currentOfficeId OR flight_src_office_id <= 0) AND onBoard = 0 AND status = 6)") //ORDER BY updatedAt DESC LIMIT 1
    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    @Query("SELECT barcode FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun observeDcUnloadingBarcodeBox(currentOfficeId: Int): Flowable<String>

    @Query("SELECT * FROM FlightBoxEntity WHERE onBoard = 1")
    fun findDcReturnBoxes(): Single<List<FlightBoxEntity>>

    @Query("SELECT COUNT(*) AS dcUnloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId) AND onBoard = 0) AS unloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId) AND onBoard = 0) AS dcUnloadingReturnCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 1) AS returnCount FROM FlightBoxEntity")
    fun dcUnloadingCongratulation(currentOfficeId: Int): Single<DcCongratulationEntity>

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.flight_dst_office_id AND FlightBoxEntity.status = 3) AS attachedCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.flight_dst_office_id AND FlightBoxEntity.onBoard = 0 AND status = 5) AS unloadedCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.flight_src_office_id AND FlightBoxEntity.onBoard = 1) AS returnCount FROM FlightOfficeEntity")
    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun loadBox(barcodes: List<String>): Single<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity ORDER BY updatedAt")
    fun observeAttachedBox(): Flowable<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3")
    fun observeFilterByOfficeIdAttachedBoxes(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

}