package ru.wb.perevozka.ui.auth.domain

import io.reactivex.Completable
import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Observable
import io.reactivex.Single

interface NumberPhoneInteractor {

    fun userPhone() : String

    @Deprecated("")
    fun checkExistAndSavePhone(phone: String) : Single<CheckExistPhoneResponse>

    fun couriersExistAndSavePhone(phone: String) : Completable

    fun observeNetworkConnected(): Observable<NetworkState>
}