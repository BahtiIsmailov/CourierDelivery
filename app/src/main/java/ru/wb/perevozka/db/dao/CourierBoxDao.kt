package ru.wb.perevozka.db.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity

@Dao
interface CourierBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBox(courierBox: CourierBoxEntity): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBoxes(courierBoxes: List<CourierBoxEntity>): Completable

    @Query("SELECT * FROM CourierBoxEntity")
    fun readAllBoxes(): Single<List<CourierBoxEntity>>

    @Query("SELECT * FROM CourierBoxEntity")
    fun observeBoxes(): Flowable<List<CourierBoxEntity>>

    @Delete
    fun deleteBox(courierBoxEntity: CourierBoxEntity): Completable

    @Delete
    fun deleteBoxes(courierBoxEntity: List<CourierBoxEntity>): Completable

    @Query("DELETE FROM CourierBoxEntity WHERE id IN (:qrCodes)")
    fun deleteBoxesByQrCode(qrCodes: List<String>): Completable

    @Query("DELETE FROM CourierBoxEntity")
    fun deleteAllBoxes()

    @Query("SELECT Office.address AS address, Office.longitude AS longitude, Office.latitude AS latitude, BoxCounter.deliveredCount AS deliveredCount, BoxCounter.fromCount AS fromCount FROM (SELECT dst_office_full_address AS address, dst_office_longitude AS longitude, dst_office_latitude AS latitude, dst_office_id AS officeId FROM CourierOrderDstOfficeLocalEntity) AS Office LEFT JOIN (SELECT COUNT(CASE WHEN deliveredAt != '' THEN 1 END) AS deliveredCount, COUNT(*) AS fromCount, dstOfficeId FROM CourierBoxEntity GROUP BY dstOfficeId) AS BoxCounter ON Office.officeId = BoxCounter.dstOfficeId")
    fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>>

}