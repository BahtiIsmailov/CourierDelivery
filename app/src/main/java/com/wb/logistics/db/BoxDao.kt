package com.wb.logistics.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface BoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable

    @Query("SELECT * FROM FlightBoxScannedEntity")
    fun observeFlightBoxScanned(): Flowable<List<FlightBoxScannedEntity>>

    @Query("SELECT * FROM FlightBoxScannedEntity WHERE barcode = :barcode")
    fun findFlightBoxScanned(barcode: String): Single<FlightBoxScannedEntity>

    @Query("SELECT * FROM FlightBoxScannedEntity WHERE barcode IN (:barcodes)")
    fun loadFlightBoxScanned(barcodes: List<String>): Single<List<FlightBoxScannedEntity>>

    @Query("DELETE FROM FlightBoxScannedEntity WHERE barcode = :barcode")
    fun deleteFlightBoxScanned(barcode: String)

    @Query("DELETE FROM FlightBoxScannedEntity")
    fun deleteAllFlightBoxScanned()

}