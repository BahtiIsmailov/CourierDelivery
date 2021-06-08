package com.wb.logistics.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
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

    @Query("SELECT * FROM UnloadedBoxEntity WHERE barcode = :barcode")
    fun findUnloadedBox(barcode: String): Single<UnloadedBoxEntity>

}