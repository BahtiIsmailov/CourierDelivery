package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ReturnBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReturnBoxEntity(returnBoxEntity: ReturnBoxEntity): Completable

    @Query("SELECT * FROM ReturnBoxEntity")
    fun observeReturnBox(): Flowable<List<ReturnBoxEntity>>

    @Query("SELECT * FROM ReturnBoxEntity WHERE current_office_id = :dstOfficeId")
    fun observeFilterByOfficeIdReturnBoxes(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>>

    @Query("SELECT * FROM ReturnBoxEntity")
    fun readReturnBox(): Single<List<ReturnBoxEntity>>

    @Query("SELECT * FROM ReturnBoxEntity WHERE barcode = :barcode")
    fun findReturnBox(barcode: String): Single<ReturnBoxEntity>

    @Query("SELECT * FROM ReturnBoxEntity WHERE barcode IN (:barcodes)")
    fun loadReturnBoxEntity(barcodes: List<String>): Single<List<ReturnBoxEntity>>

    @Query("DELETE FROM ReturnBoxEntity WHERE barcode = :barcode")
    fun deleteReturnBox(barcode: String)

    @Delete
    fun deleteReturnBoxEntity(returnBoxEntity: ReturnBoxEntity): Completable

    @Query("DELETE FROM ReturnBoxEntity")
    fun deleteAllUnloadedBox()

}