package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface AttachedBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttachedBoxes(attachedBoxesEntity: List<AttachedBoxEntity>): Completable

    @Query("SELECT * FROM AttachedBoxEntity")
    fun observeAttachedBox(): Flowable<List<AttachedBoxEntity>>

    @Query("SELECT * FROM AttachedBoxEntity WHERE dst_office_id = :dstOfficeId")
    fun observeFilterByOfficeIdAttachedBoxes(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>>

    @Query("SELECT * FROM AttachedBoxEntity")
    fun readAttachedBox(): Single<List<AttachedBoxEntity>>

    @Query("SELECT * FROM AttachedBoxEntity WHERE barcode = :barcode")
    fun findAttachedBox(barcode: String): Single<AttachedBoxEntity>

    @Query("SELECT * FROM AttachedBoxEntity WHERE barcode IN (:barcodes)")
    fun loadAttachedBox(barcodes: List<String>): Single<List<AttachedBoxEntity>>

    @Delete
    fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    @Query("DELETE FROM AttachedBoxEntity")
    fun deleteAllAttachedBox()

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, isUnloading AS isUnloading, (SELECT COUNT(*) FROM AttachedBoxEntity WHERE FlightOfficeEntity.office_id = AttachedBoxEntity.dst_office_id) AS attachedCount, (SELECT COUNT(*) FROM ReturnBoxEntity WHERE FlightOfficeEntity.office_id = ReturnBoxEntity.current_office_id) AS returnCount, (SELECT COUNT(*) FROM UnloadedBoxEntity WHERE FlightOfficeEntity.office_id = UnloadedBoxEntity.current_office_id) AS unloadedCount FROM FlightOfficeEntity")
    fun groupAttachedBoxByDstAddress(): Single<List<AttachedBoxGroupByOfficeEntity>>

    @Query("SELECT COUNT(isUnloading) AS pickPointCount, (SELECT COUNT(*) FROM AttachedBoxEntity) AS attachedCount, (SELECT COUNT(*) FROM UnloadedBoxEntity) AS unloadedCount FROM FlightOfficeEntity WHERE isUnloading = :isUnloading")
    fun groupAttachedBox(isUnloading: Boolean = true): Single<AttachedBoxResultEntity>

}