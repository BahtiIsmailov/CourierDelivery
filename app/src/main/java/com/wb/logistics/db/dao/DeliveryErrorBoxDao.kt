package com.wb.logistics.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.logistics.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface DeliveryErrorBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(deliveryErrorBoxEntity: DeliveryErrorBoxEntity): Completable

    @Query("SELECT * FROM DeliveryErrorBoxEntity WHERE currentOfficeId = :currentOfficeId")
    fun findDeliveryErrorBoxByOfficeId(currentOfficeId: Int): Single<List<DeliveryErrorBoxEntity>>

}