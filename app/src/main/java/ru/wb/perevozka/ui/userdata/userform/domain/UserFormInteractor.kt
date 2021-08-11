package ru.wb.perevozka.ui.userdata.userform.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.perevozka.network.monitor.NetworkState

interface UserFormInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun couriersForm(phone: String): Completable
}