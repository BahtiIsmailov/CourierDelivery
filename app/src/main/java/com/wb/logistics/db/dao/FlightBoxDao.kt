package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface FlightBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxes(flightMatchingBoxes: List<FlightBoxEntity>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBox(flightMatchingBox: FlightBoxEntity): Completable

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode")
    fun findFlightBox(barcode: String): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND flight_dst_office_id = :currentOfficeId AND onBoard = 0")
    fun findUnloadedFlightBox(barcode: String, currentOfficeId: Int): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE flight_dst_office_id = :currentOfficeId AND onBoard = 0")
    fun observeUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode AND flight_src_office_id = :currentOfficeId AND onBoard = 1")
    fun findReturnedFlightBox(barcode: String, currentOfficeId: Int): Single<FlightBoxEntity>

    @Query("SELECT * FROM FlightBoxEntity WHERE flight_src_office_id = :currentOfficeId AND onBoard = 1")
    fun observeReturnedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<FlightBoxEntity>>

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode IN (:barcodes)")
    fun findReturnedFlightBoxes(barcodes: List<String>): Single<List<FlightBoxEntity>>

    @Delete
    fun deleteReturnFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable

    @Query("DELETE FROM FlightBoxEntity")
    fun deleteAllFlightBox()

}