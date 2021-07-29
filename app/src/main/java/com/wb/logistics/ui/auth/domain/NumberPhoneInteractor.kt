package com.wb.logistics.ui.auth.domain

import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.monitor.NetworkState
import io.reactivex.Observable
import io.reactivex.Single

interface NumberPhoneInteractor {

    fun userPhone() : String

    fun checkExistAndSavePhone(phone: String) : Single<CheckExistPhoneResponse>

    fun observeNetworkConnected(): Observable<NetworkState>
}