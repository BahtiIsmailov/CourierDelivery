package ru.wb.go.ui.flightdeliveries.domain

import ru.wb.go.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.go.network.monitor.NetworkState
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