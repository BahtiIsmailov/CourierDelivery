package ru.wb.perevozka.ui.userdata.userform.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
import ru.wb.perevozka.network.monitor.NetworkState

interface UserFormInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun courierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable
}