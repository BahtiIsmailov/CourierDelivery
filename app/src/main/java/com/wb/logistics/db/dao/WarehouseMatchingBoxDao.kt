package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface WarehouseMatchingBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBoxes(matchingBoxes: List<WarehouseMatchingBoxEntity>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBox(matchingBox: WarehouseMatchingBoxEntity): Completable

    @Delete
    fun deleteMatchingBox(matchingBox: WarehouseMatchingBoxEntity): Completable

    @Query("SELECT * FROM WarehouseMatchingBoxEntity WHERE barcode = :barcode")
    fun findMatchingBox(barcode: String): Single<WarehouseMatchingBoxEntity>

    @Query("DELETE FROM WarehouseMatchingBoxEntity")
    fun deleteAllMatchingBox()

}