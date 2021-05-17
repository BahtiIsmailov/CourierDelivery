package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface FlightDao {

    //==============================================================================================
    //flight
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlight(flightEntity: FlightEntity): Completable

    @Transaction
    @Query("SELECT * FROM FlightEntity")
    fun observeFlight(): Flowable<FlightDataEntity>

    @Query("SELECT * FROM FlightEntity")
    fun readFlight(): Single<FlightEntity>

    @Transaction
    @Query("SELECT * FROM FlightEntity")
    fun readFlightData(): Single<FlightDataEntity>

    @Delete
    fun deleteFlight(flightEntity: FlightEntity): Completable

    @Query("DELETE FROM FlightEntity")
    fun deleteAllFlight()

    //==============================================================================================
    //flight office
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightOffice(flightOfficeEntity: List<FlightOfficeEntity>): Completable

    @Query("UPDATE FlightOfficeEntity SET isUnloading=:isUnloading, notUnloadingCause=:notUnloadingCause WHERE office_id = :dstOfficeId")
    fun changeFlightOfficeUnloading(dstOfficeId: Int, isUnloading: Boolean, notUnloadingCause: String): Completable

    //==============================================================================================
    //boxes attached to flight
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxes(flightBoxes: List<FlightBoxEntity>): Completable

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode")
    fun findFlightBox(barcode: String): Single<FlightBoxEntity>

    @Query("DELETE FROM FlightBoxEntity")
    fun deleteAllFlightBoxes()

    //==============================================================================================
    //matching boxes
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBoxes(matchingBoxes: List<MatchingBoxEntity>): Completable

    @Query("SELECT * FROM MatchingBoxEntity WHERE barcode = :barcode")
    fun findMatchingBox(barcode: String): Single<MatchingBoxEntity>

    @Query("DELETE FROM MatchingBoxEntity")
    fun deleteAllMatchingBox()

    //==============================================================================================
    //box balance await
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable

    @Query("SELECT * FROM AttachedBoxBalanceAwaitEntity")
    fun observeFlightBoxBalanceAwait(): Flowable<List<AttachedBoxBalanceAwaitEntity>>

    @Query("SELECT * FROM AttachedBoxBalanceAwaitEntity")
    fun flightBoxBalanceAwait(): Single<List<AttachedBoxBalanceAwaitEntity>>

    @Delete
    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable

    @Query("DELETE FROM AttachedBoxBalanceAwaitEntity")
    fun deleteAllFlightBoxBalanceAwait()

}