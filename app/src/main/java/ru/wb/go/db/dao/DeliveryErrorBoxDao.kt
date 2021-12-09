package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.wb.go.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import ru.wb.go.db.entity.deliveryerrorbox.DeliveryUnloadingErrorBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface DeliveryErrorBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(deliveryErrorBoxEntity: DeliveryErrorBoxEntity): Completable

    @Query("SELECT * FROM DeliveryErrorBoxEntity WHERE currentOfficeId = :currentOfficeId")
    fun findDeliveryErrorBoxByOfficeId(currentOfficeId: Int): Single<List<DeliveryErrorBoxEntity>>


    @Query("REPLACE INTO DeliveryErrorBoxEntity (barcode, currentOfficeId) SELECT barcode, :currentOfficeId AS currentOfficeId FROM FlightBoxEntity WHERE dst_office_id = :currentOfficeId AND onBoard = 1 AND status = 3")
    fun insertNotUnloadingBoToDeliveryErrorByOfficeId(currentOfficeId: Int): Completable

    @Query("UPDATE FlightBoxEntity SET updatedAt = :updatedAt, onBoard = :onBoard, status = :status WHERE dst_office_id = :currentOfficeId")
    fun changeNotUnloadingBoxToFlightBoxesByOfficeId(
        currentOfficeId: Int,
        updatedAt: String,
        onBoard: Boolean,
        status: Int,
    ): Completable

    @Query("SELECT  FlightBox.barcode AS barcode, FlightBox.dst_office_id AS dstOfficeId, FlightBox.updatedAt AS updatedAt, FlightBox.dst_office_full_address as fullAddress, FlightBox.onBoard as onBoard, DeliveryError.currentOfficeId AS errorOfficeId, DeliveryError.fullAddress  as errorOfficeFullAddress FROM (SELECT * FROM FlightBoxEntity WHERE  dst_office_id = :currentOfficeId) AS FlightBox LEFT JOIN  (SELECT * FROM DeliveryErrorBoxEntity LEFT JOIN FlightOfficeEntity ON DeliveryErrorBoxEntity.currentOfficeId = FlightOfficeEntity.office_id) AS DeliveryError ON FlightBox.barcode = DeliveryError.barcode")
    fun observeDeliveryUnloadedFlightBoxesByOfficeId(currentOfficeId: Int): Flowable<List<DeliveryUnloadingErrorBoxEntity>>

    @Query("DELETE FROM DeliveryErrorBoxEntity WHERE barcode = :barcode")
    fun deleteByBarcode(barcode: String): Completable

}