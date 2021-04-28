package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxGroupByAddressEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface UnloadingBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnloadingBox(unloadedBoxEntity: UnloadedBoxEntity): Completable

    @Query("SELECT * FROM UnloadedBoxEntity")
    fun observeUnloadingBox(): Flowable<List<UnloadedBoxEntity>>

    @Query("SELECT * FROM UnloadedBoxEntity WHERE current_office_id = :dstOfficeId")
    fun observeFilterByOfficeIdAttachedBoxes(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>>

    @Query("SELECT * FROM UnloadedBoxEntity")
    fun readUnloadingBox(): Single<List<UnloadedBoxEntity>>

    @Query("SELECT * FROM UnloadedBoxEntity WHERE barcode = :barcode")
    fun findUnloadedBox(barcode: String): Single<UnloadedBoxEntity>

    @Query("SELECT * FROM UnloadedBoxEntity WHERE barcode IN (:barcodes)")
    fun loadUnloadedBox(barcodes: List<String>): Single<List<UnloadedBoxEntity>>

    @Query("DELETE FROM UnloadedBoxEntity WHERE barcode = :barcode")
    fun deleteUnloadedBox(barcode: String)

    @Delete
    fun deleteUnloadedBoxEntity(unloadedBoxEntity: UnloadedBoxEntity): Completable

    @Query("DELETE FROM UnloadedBoxEntity")
    fun deleteAllUnloadedBox()

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM AttachedBoxEntity WHERE FlightOfficeEntity.office_id = AttachedBoxEntity.dst_office_id) AS count FROM FlightOfficeEntity")
    fun groupByDstAddressUnloadedBox(): Single<List<UnloadedBoxGroupByAddressEntity>>

}