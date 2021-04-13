package com.wb.logistics.db

import androidx.room.*
import com.wb.logistics.db.entity.boxtoflight.FlightBoxBalanceAwaitEntity
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
    fun deleteFlight(flightEntity: FlightEntity)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxes(flightBoxes: List<FlightBoxEntity>): Completable

    @Query("SELECT * FROM FlightBoxEntity WHERE barcode = :barcode")
    fun findFlightBox(barcode: String): Single<FlightBoxEntity>

    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBoxes(matchingBoxes: List<MatchingBoxEntity>): Completable

    @Query("SELECT * FROM MatchingBoxEntity WHERE barcode = :barcode")
    fun findMatchingBox(barcode: String): Single<MatchingBoxEntity>

    //==============================================================================================
    //баланс отсканированых коробок которые ожидают отправки на сервер
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxBalanceAwait(flightBoxBalanceEntity: FlightBoxBalanceAwaitEntity): Completable

    @Query("SELECT * FROM FlightBoxBalanceAwaitEntity")
    fun observeFlightBoxBalanceAwait(): Flowable<List<FlightBoxBalanceAwaitEntity>>

    @Query("SELECT * FROM FlightBoxBalanceAwaitEntity")
    fun flightBoxBalanceAwait(): Single<List<FlightBoxBalanceAwaitEntity>>

    @Delete
    fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: FlightBoxBalanceAwaitEntity): Completable

}