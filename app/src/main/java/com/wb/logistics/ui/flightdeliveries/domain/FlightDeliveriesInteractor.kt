package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import io.reactivex.Completable
import io.reactivex.Single

interface FlightDeliveriesInteractor {

    fun updateFlight(): Completable

    fun flightId(): Single<String>

    fun getDeliveryBoxesGroupByOffice() : Single<List<DeliveryBoxGroupByOfficeEntity>>

    fun getNotDelivered() : Single<Int>

    fun switchScreenToDcUnloading(): Completable

    fun updatePvzAttachedBoxes(): Completable

}