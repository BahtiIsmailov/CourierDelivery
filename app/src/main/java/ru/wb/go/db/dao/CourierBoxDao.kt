package ru.wb.go.db.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingBoxCounterResult
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInitLastBoxResult

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

    @Query("SELECT Office.address AS address, Office.longitude AS longitude, Office.latitude AS latitude, Visited.visitedAt AS visitedAt, Visited.isUnloaded AS isUnloaded, BoxCounter.deliveredCount AS deliveredCount, BoxCounter.fromCount AS fromCount FROM (SELECT dst_office_full_address AS address, dst_office_longitude AS longitude, dst_office_latitude AS latitude, dst_office_id AS officeId, dst_office_visited_at AS visitedAt FROM CourierOrderDstOfficeLocalEntity) AS Office LEFT JOIN (SELECT COUNT(CASE WHEN deliveredAt != '' THEN 1 END) AS deliveredCount, COUNT(*) AS fromCount, dstOfficeId FROM CourierBoxEntity GROUP BY dstOfficeId) AS BoxCounter ON Office.officeId = BoxCounter.dstOfficeId LEFT JOIN(SELECT visited_office_dst_office_id AS visitedOfficeId, COALESCE(visited_office_visited_at, '') AS visitedAt , COALESCE(visited_office_is_unload, 0) AS isUnloaded FROM CourierOrderVisitedOfficeLocalEntity) AS Visited ON Office.officeId = Visited.visitedOfficeId")
    fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>>

    @Query("SELECT * FROM CourierBoxEntity WHERE dstOfficeId = :officeId")
    fun readAllLoadingBoxesByOfficeId(officeId: Int): Single<List<CourierBoxEntity>>

    @Query("SELECT * FROM CourierBoxEntity WHERE dstOfficeId = :officeId")
    fun readAllUnloadingBoxesByOfficeId(officeId: Int): Single<List<CourierBoxEntity>>

    @Query("SELECT COALESCE(id, '') AS id, COALESCE(address, '') AS address FROM CourierBoxEntity WHERE dstOfficeId = :officeId AND deliveredAt != '' ORDER BY deliveredAt DESC LIMIT 1")
    fun readInitLastUnloadingBox(officeId: Int): Single<CourierUnloadingInitLastBoxResult>

    @Query("SELECT deliveredCount AS unloadedCount, fromCount AS fromCount FROM (SELECT SUM(CASE WHEN deliveredAt != '' THEN 1 ELSE 0 END) AS deliveredCount, COUNT(*) AS fromCount FROM CourierBoxEntity WHERE dstOfficeId = :officeId) AS Counter")
    fun readUnloadingBoxCounter(officeId: Int): Single<CourierUnloadingBoxCounterResult>

    @Query("SELECT deliveredCount AS unloadedCount, fromCount AS fromCount FROM (SELECT SUM(CASE WHEN deliveredAt != '' THEN 1 ELSE 0 END) AS deliveredCount, COUNT(*) AS fromCount FROM CourierBoxEntity WHERE dstOfficeId = :officeId) AS Counter")
    fun observeCounterBox(officeId: Int): Flowable<CourierUnloadingBoxCounterResult>

    @Query("SELECT Counter.deliveredCount AS unloadedCount, Counter.fromCount AS fromCount FROM (SELECT minPrice FROM CourierOrderLocalEntity) AS CourierOrder, (SELECT SUM(CASE WHEN deliveredAt != '' THEN 1 ELSE 0 END) AS deliveredCount, COUNT(*) AS fromCount FROM CourierBoxEntity) AS Counter")
    fun completeDeliveryResult(): Single<CompleteDeliveryResult>

    @Query("SELECT * FROM CourierBoxEntity WHERE dstOfficeId IN (SELECT visited_office_dst_office_id FROM CourierOrderVisitedOfficeLocalEntity WHERE visited_office_is_unload = 0)")
    fun readNotUnloadingBoxes(): Single<List<CourierBoxEntity>>

    @Query("DELETE FROM CourierOrderVisitedOfficeLocalEntity")
    fun deleteAllVisitedOffices()

}