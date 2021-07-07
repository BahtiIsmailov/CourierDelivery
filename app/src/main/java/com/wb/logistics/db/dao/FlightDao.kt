package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
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
    fun observeFlightData(): Flowable<FlightDataEntity>

    @Query("SELECT * FROM FlightEntity")
    fun readFlight(): Single<FlightEntity>

    @Query("DELETE FROM FlightEntity")
    fun deleteAllFlight()

    //==============================================================================================
    //flight office
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightOffice(flightOfficeEntity: List<FlightOfficeEntity>): Completable

    @Query("SELECT * FROM FlightOfficeEntity WHERE office_id = :id")
    fun findFlightOffice(id: Int): Single<FlightOfficeEntity>

    @Query("UPDATE FlightOfficeEntity SET visitedAt = :visitedAt WHERE office_id = :id")
    fun updateFlightOfficeVisited(visitedAt: String, id: Int): Completable

    @Query("DELETE FROM FlightOfficeEntity")
    fun deleteAllFlightOffices()

}