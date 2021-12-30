package ru.wb.go.ui.courierdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierDataInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable
    fun getCourierDocuments(): Single<CourierDocumentsEntity>
}