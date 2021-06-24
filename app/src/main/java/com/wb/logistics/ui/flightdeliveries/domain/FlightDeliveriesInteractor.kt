package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import io.reactivex.Completable
import io.reactivex.Single

interface FlightDeliveriesInteractor {

    fun flightId(): Single<String>

    fun getDeliveryBoxesGroupByOffice() : Single<List<DeliveryBoxGroupByOfficeEntity>>

    fun getAttachedBoxes(): Single<Int>

    fun switchScreenDcUnloading(): Completable

    fun updatePvzAttachedBoxes(): Completable

}