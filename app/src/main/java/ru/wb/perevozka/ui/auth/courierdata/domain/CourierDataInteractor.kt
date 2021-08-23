package ru.wb.perevozka.ui.auth.courierdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
import ru.wb.perevozka.network.monitor.NetworkState

interface CourierDataInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun courierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable
}