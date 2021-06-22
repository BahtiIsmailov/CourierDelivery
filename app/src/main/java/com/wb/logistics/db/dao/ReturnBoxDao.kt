package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.returnboxes.ReturnBoxByAddressEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ReturnBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReturnBoxEntity(returnBoxEntity: ReturnBoxEntity): Completable

    @Query("SELECT * FROM ReturnBoxEntity WHERE current_office_id = :dstOfficeId")
    fun observeFilterByOfficeIdReturnBoxes(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>>

    @Query("SELECT * FROM ReturnBoxEntity WHERE barcode = :barcode")
    fun findReturnBox(barcode: String): Single<ReturnBoxEntity>

    @Query("SELECT * FROM ReturnBoxEntity WHERE barcode IN (:barcodes)")
    fun findReturnBoxes(barcodes: List<String>): Single<List<ReturnBoxEntity>>

    @Delete
    fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity): Completable

    @Delete
    fun deleteReturnBoxes(returnBoxesEntity: List<ReturnBoxEntity>): Completable

    @Query("SELECT barcode AS barcode, updatedAt AS updatedAt, (SELECT fullAddress FROM FlightOfficeEntity WHERE ReturnBoxEntity.current_office_id = FlightOfficeEntity.office_id) AS address FROM ReturnBoxEntity WHERE current_office_id = :dstOfficeId")
    fun groupByDstAddressReturnBox(dstOfficeId: Int): Single<List<ReturnBoxByAddressEntity>>

}