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

}