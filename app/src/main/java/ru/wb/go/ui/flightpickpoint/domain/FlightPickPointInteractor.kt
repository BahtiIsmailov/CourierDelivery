package ru.wb.go.ui.flightpickpoint.domain

import ru.wb.go.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface FlightPickPointInteractor {

    fun flightId(): Single<Int>

    fun getAttachedBoxesGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>>

    fun createTTN(): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}