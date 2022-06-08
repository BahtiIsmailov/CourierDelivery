package ru.wb.go.ui.auth.domain

import io.reactivex.Completable
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Observable

interface NumberPhoneInteractor {

    fun userPhone() : String

    fun couriersExistAndSavePhone(phone: String) : Completable

    fun observeNetworkConnected(): Observable<NetworkState>
}