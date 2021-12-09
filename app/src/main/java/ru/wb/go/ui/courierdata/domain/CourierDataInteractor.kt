package ru.wb.go.ui.courierdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierDataInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun courierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable
}