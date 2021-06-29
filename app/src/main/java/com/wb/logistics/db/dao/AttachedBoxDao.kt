package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
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

    @Delete
    fun deleteAttachedBoxes(attachedBoxesEntity: List<AttachedBoxEntity>): Completable

    @Query("DELETE FROM AttachedBoxEntity")
    fun deleteAllAttachedBox()

    @Query("SELECT office_id AS officeId, office_name AS officeName, fullAddress AS dstFullAddress, (SELECT COUNT(*) FROM AttachedBoxEntity WHERE FlightOfficeEntity.office_id = AttachedBoxEntity.dst_office_id) AS attachedCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.flight_src_office_id AND FlightBoxEntity.onBoard = 1) AS returnCount, (SELECT COUNT(*) FROM FlightBoxEntity WHERE FlightOfficeEntity.office_id = FlightBoxEntity.flight_dst_office_id AND FlightBoxEntity.onBoard = 0) AS unloadedCount FROM FlightOfficeEntity")
    fun groupDeliveryBoxByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>>

}