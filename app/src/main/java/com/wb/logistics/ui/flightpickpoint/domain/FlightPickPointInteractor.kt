package com.wb.logistics.ui.flightpickpoint.domain

import com.wb.logistics.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import com.wb.logistics.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface FlightPickPointInteractor {

    fun flightId(): Single<Int>

    fun getAttachedBoxesGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>>

    fun createTTN(): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}