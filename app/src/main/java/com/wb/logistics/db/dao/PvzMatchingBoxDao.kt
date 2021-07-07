package com.wb.logistics.db.dao

import androidx.room.*
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface PvzMatchingBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBoxes(box: List<PvzMatchingBoxEntity>): Completable

    @Query("SELECT * FROM PvzMatchingBoxEntity")
    fun readBoxes(): Single<List<PvzMatchingBoxEntity>>

    @Query("SELECT * FROM PvzMatchingBoxEntity WHERE barcode = :barcode")
    fun findBox(barcode: String): Single<PvzMatchingBoxEntity>

    @Delete
    fun deletePvzMatchingBox(pvzMatchingBoxEntity: PvzMatchingBoxEntity): Completable

    @Query("DELETE FROM PvzMatchingBoxEntity")
    fun deleteAllBox()

    @Query("SELECT * FROM PvzMatchingBoxEntity WHERE src_office_id = :currentOfficeId")
    fun observePvzMatchingBoxByOfficeId(currentOfficeId: Int): Flowable<List<PvzMatchingBoxEntity>>

}