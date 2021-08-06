package ru.wb.perevozka.ui.auth.domain

import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Observable
import io.reactivex.Single

interface NumberPhoneInteractor {

    fun userPhone() : String

    fun checkExistAndSavePhone(phone: String) : Single<CheckExistPhoneResponse>

    fun observeNetworkConnected(): Observable<NetworkState>
}