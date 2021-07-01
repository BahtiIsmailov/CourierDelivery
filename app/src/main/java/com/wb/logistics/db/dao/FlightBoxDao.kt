package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
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

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND onBoard = 0")
    fun findUnloadedFlightBox(barcode: String): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 0")
    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

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

    @Query("SELECT barcode AS barcode FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 1")
    fun findDcReturnHandleBoxes(currentOfficeId: Int): Single<List<DcReturnHandleBarcodeEntity>>

    @Query("SELECT barcode AS barcode, updatedAt AS updatedAt FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0")
    fun findDcUnloadedBarcodes(currentOfficeId: Int): Single<List<DcUnloadingBarcodeEntity>>

    @Delete
    fun deleteFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    @Delete
    fun deleteFlightBox(flightBoxesEntity: FlightBoxEntity): Completable

    @Query("DELETE FROM FlightBoxEntity")
    fun deleteAllFlightBox()

    @Query("SELECT barcode AS barcode, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0) AS dcUnloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 1) AS dcReturnCount FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun observeDcUnloadingScanBox(currentOfficeId: Int): Flowable<DcUnloadingScanBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 1")
    fun findDcReturnBoxes(currentOfficeId: Int): Single<List<FlightBoxEntity>>

    @Query("SELECT COUNT(*) AS dcUnloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0) AS unloadingCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 0) AS dcUnloadingReturnCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE (flight_dst_office_id = :currentOfficeId OR flight_dst_office_id <= 0) AND onBoard = 1) AS returnCount FROM FlightBoxEntity")
    fun dcUnloadingCongratulation(currentOfficeId: Int): Single<DcCongratulationEntity>

}