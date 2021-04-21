package com.wb.logistics.db

import androidx.room.*
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface BoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScannedBox(flightBoxScannedEntity: ScannedBoxEntity): Completable

    @Query("SELECT * FROM ScannedBoxEntity")
    fun observeScannedBox(): Flowable<List<ScannedBoxEntity>>

    @Query("SELECT * FROM ScannedBoxEntity")
    fun readScannedBox(): Single<List<ScannedBoxEntity>>

    @Query("SELECT * FROM ScannedBoxEntity WHERE barcode = :barcode")
    fun findScannedBox(barcode: String): Single<ScannedBoxEntity>

    @Query("SELECT * FROM ScannedBoxEntity WHERE barcode IN (:barcodes)")
    fun loadScannedBox(barcodes: List<String>): Single<List<ScannedBoxEntity>>

    @Query("DELETE FROM ScannedBoxEntity WHERE barcode = :barcode")
    fun deleteScannedBox(barcode: String)

    @Delete
    fun deleteScannedBox(scannedBoxEntity: ScannedBoxEntity): Completable

    @Query("DELETE FROM ScannedBoxEntity")
    fun deleteAllScannedBox()

    @Query("SELECT dstFullAddress, COUNT(*) AS count FROM ScannedBoxEntity GROUP BY dstFullAddress")
    fun groupByOffice(): Single<List<ScannedBoxGroupByAddressEntity>>

//    @Query("SELECT dstFullAddress, COUNT(*) AS count FROM ScannedBoxEntity GROUP BY dstFullAddress")
//    fun groupByDstAddress(): Single<List<ScannedBoxGroupByAddressEntity>>

    @Query("SELECT fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM ScannedBoxEntity WHERE FlightOfficeEntity.office_id = ScannedBoxEntity.dst_office_id) AS count FROM FlightOfficeEntity")
    fun groupByDstAddressScannedBox(): Single<List<ScannedBoxGroupByAddressEntity>>

}