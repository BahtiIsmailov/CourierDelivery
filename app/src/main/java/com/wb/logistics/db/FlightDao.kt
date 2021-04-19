package com.wb.logistics.db

import androidx.room.*
import com.wb.logistics.db.entity.boxtoflight.ScannedBoxBalanceAwaitEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightOffice(flightOfficeEntity: List<FlightOfficeEntity>): Completable

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
    fun insertFlightBoxBalanceAwait(flightBoxBalanceEntity: ScannedBoxBalanceAwaitEntity): Completable

    @Query("SELECT * FROM ScannedBoxBalanceAwaitEntity")
    fun observeFlightBoxBalanceAwait(): Flowable<List<ScannedBoxBalanceAwaitEntity>>

    @Query("SELECT * FROM ScannedBoxBalanceAwaitEntity")
    fun flightBoxBalanceAwait(): Single<List<ScannedBoxBalanceAwaitEntity>>

    @Delete
    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: ScannedBoxBalanceAwaitEntity): Completable

    @Query("DELETE FROM ScannedBoxBalanceAwaitEntity")
    fun deleteAllFlightBoxBalanceAwait()

}