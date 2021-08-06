package ru.wb.perevozka.ui.flightpickpoint.domain

import ru.wb.perevozka.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface FlightPickPointInteractor {

    fun flightId(): Single<Int>

    fun getAttachedBoxesGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>>

    fun createTTN(): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}