package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
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

    @Query("DELETE FROM FlightEntity")
    fun deleteAllFlight()

    //==============================================================================================
    //flight office
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightOffice(flightOfficeEntity: List<FlightOfficeEntity>): Completable

    @Query("UPDATE FlightOfficeEntity SET isUnloading=:isUnloading, notUnloadingCause=:notUnloadingCause WHERE office_id = :dstOfficeId")
    fun changeFlightOfficeUnloading(
        dstOfficeId: Int,
        isUnloading: Boolean,
        notUnloadingCause: String,
    ): Completable

    @Query("SELECT * FROM FlightOfficeEntity WHERE office_id = :id")
    fun findFlightOffice(id: Int): Single<FlightOfficeEntity>

    //==============================================================================================
    //matching boxes
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBoxes(matchingBoxes: List<MatchingBoxEntity>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBox(matchingBox: MatchingBoxEntity): Completable

    @Delete
    fun deleteMatchingBox(matchingBox: MatchingBoxEntity): Completable

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
    fun attachedBoxesBalanceAwait(): Single<List<AttachedBoxBalanceAwaitEntity>>

    @Delete
    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: AttachedBoxBalanceAwaitEntity): Completable

    @Query("DELETE FROM AttachedBoxBalanceAwaitEntity")
    fun deleteAllFlightBoxBalanceAwait()

}