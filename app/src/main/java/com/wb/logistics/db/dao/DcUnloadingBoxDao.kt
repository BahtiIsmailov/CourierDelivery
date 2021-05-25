package com.wb.logistics.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.logistics.db.entity.dcunloadedboxes.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface DcUnloadingBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDcUnloadingBox(dcUnloadedBoxEntity: DcUnloadedBoxEntity): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDcUnloadingReturnBox(dcUnloadedReturnBoxEntity: DcUnloadedReturnBoxEntity): Completable

    @Query("SELECT * FROM DcUnloadedBoxEntity WHERE barcode = :barcode")
    fun findDcUnloadedBox(barcode: String): Single<DcUnloadedBoxEntity>

    @Query("SELECT barcode AS barcode FROM AttachedBoxEntity UNION SELECT barcode AS barcode FROM ReturnBoxEntity")
    fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>>

    @Query("SELECT barcode AS barcode FROM DcUnloadedBoxEntity UNION SELECT barcode AS barcode FROM DcUnloadedReturnBoxEntity")
    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>>

    @Query("SELECT COUNT(*) AS dcUnloadingCount, '' as barcode, (SELECT COUNT(*) FROM AttachedBoxEntity) AS attachedCount, (SELECT COUNT(*) FROM ReturnBoxEntity) AS returnCount FROM DcUnloadedBoxEntity")
    fun observeDcUnloadingScanBox(): Flowable<DcUnloadingScanBoxEntity>

    @Query("SELECT COUNT(*) AS dcUnloadingCount, (SELECT COUNT(*) FROM AttachedBoxEntity) AS attachedCount, (SELECT COUNT(*) FROM DcUnloadedReturnBoxEntity) AS dcUnloadingReturnCount, (SELECT COUNT(*) FROM ReturnBoxEntity) AS returnCount FROM DcUnloadedBoxEntity")
    fun congratulation(): Single<DcCongratulationEntity>

    @Query("SELECT barcode AS barcode, updatedAt as updatedAt, '' AS dstFullAddress FROM AttachedBoxEntity UNION SELECT barcode AS barcode, updatedAt as updatedAt, (SELECT fullAddress FROM FlightOfficeEntity WHERE FlightOfficeEntity.office_id = ReturnBoxEntity.current_office_id) AS dstFullAddress FROM ReturnBoxEntity")
    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

}