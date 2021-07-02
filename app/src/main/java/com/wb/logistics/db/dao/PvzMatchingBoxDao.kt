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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBox(box: PvzMatchingBoxEntity): Completable

    @Query("SELECT * FROM PvzMatchingBoxEntity")
    fun readBoxes(): Single<List<PvzMatchingBoxEntity>>

    @Delete
    fun deleteBox(box: PvzMatchingBoxEntity): Completable

    @Query("SELECT * FROM PvzMatchingBoxEntity WHERE barcode = :barcode")
    fun findBox(barcode: String): Single<PvzMatchingBoxEntity>

    @Query("DELETE FROM PvzMatchingBoxEntity")
    fun deleteAllBox()

    @Query("SELECT * FROM PvzMatchingBoxEntity WHERE pvz_match_src_office_id = :currentOfficeId")
    fun observePvzMatchingBoxByOfficeId(currentOfficeId: Int): Flowable<List<PvzMatchingBoxEntity>>

}