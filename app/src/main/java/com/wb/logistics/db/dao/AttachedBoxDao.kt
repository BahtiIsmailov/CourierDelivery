package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByAddressEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface AttachedBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScannedBox(attachedBoxEntity: AttachedBoxEntity): Completable

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

    @Query("DELETE FROM AttachedBoxEntity WHERE barcode = :barcode")
    fun deleteAttachedBox(barcode: String)

    @Delete
    fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity): Completable

    @Query("DELETE FROM AttachedBoxEntity")
    fun deleteAllAttachedBox()

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM AttachedBoxEntity WHERE FlightOfficeEntity.office_id = AttachedBoxEntity.dst_office_id) AS redoCount, (SELECT COUNT(*) FROM ReturnBoxEntity WHERE FlightOfficeEntity.office_id = ReturnBoxEntity.current_office_id) AS undoCount FROM FlightOfficeEntity")
    fun groupAttachedBoxByDstAddress(): Single<List<AttachedBoxGroupByAddressEntity>>

}