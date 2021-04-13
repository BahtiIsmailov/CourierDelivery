package com.wb.logistics.db

import androidx.room.*
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

    @Delete
    fun deleteFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable

    @Query("DELETE FROM FlightBoxScannedEntity")
    fun deleteAllFlightBoxScanned()

}