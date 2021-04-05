package com.wb.logistics.db

import androidx.room.*
import com.wb.logistics.db.entity.boxesfromflight.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface FlightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlight(flightEntity: FlightEntity): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightOffice(flightOfficeEntity: List<FlightOfficeEntity>): Completable

    @Transaction
    @Query("SELECT * FROM FlightEntity")
    fun readFlight(): Flowable<FlightDataEntity>

    @Delete
    fun deleteFlight(flightEntity: FlightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxes(flightBoxes: List<FlightBoxEntity>): Completable

    @Query("SELECT * FROM FlightBoxEntity")
    fun readFlightBoxes(): Flowable<List<FlightBoxEntity>>

}