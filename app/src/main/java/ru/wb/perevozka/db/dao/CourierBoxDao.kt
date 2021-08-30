package ru.wb.perevozka.db.dao

import androidx.room.*
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import ru.wb.perevozka.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.perevozka.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.unload.UnloadingTookAndPickupCountEntity
import ru.wb.perevozka.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity
import ru.wb.perevozka.ui.dcunloading.domain.DcUnloadingCounterEntity
import ru.wb.perevozka.ui.splash.domain.AppDeliveryResult
import ru.wb.perevozka.ui.unloadingcongratulation.domain.DeliveryResult
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity

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

    @Query("DELETE FROM CourierBoxEntity WHERE qrcode IN (:qrCodes)")
    fun deleteBoxesByQrCode(qrCodes: List<String>): Completable

    @Query("DELETE FROM CourierBoxEntity")
    fun deleteAllBoxes()

}