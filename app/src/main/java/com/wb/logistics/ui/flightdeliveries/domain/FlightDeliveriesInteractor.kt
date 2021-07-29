package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface FlightDeliveriesInteractor {

    fun updateFlight(): Completable

    fun flightId(): Single<String>

    fun getDeliveryBoxesGroupByOffice() : Single<List<DeliveryBoxGroupByOfficeEntity>>

    fun getNotDelivered() : Single<Int>

    fun switchScreenToDcUnloading(): Completable

    fun updatePvzAttachedBoxes(): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}