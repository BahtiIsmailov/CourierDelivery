package com.wb.logistics.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import io.reactivex.Completable

@Dao
interface WarehouseMatchingBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchingBoxes(matchingBoxes: List<WarehouseMatchingBoxEntity>): Completable

    @Query("DELETE FROM WarehouseMatchingBoxEntity WHERE barcode = :barcode")
    fun deleteByBarcode(barcode: String): Completable

    @Query("DELETE FROM WarehouseMatchingBoxEntity")
    fun deleteAllMatchingBox()

}