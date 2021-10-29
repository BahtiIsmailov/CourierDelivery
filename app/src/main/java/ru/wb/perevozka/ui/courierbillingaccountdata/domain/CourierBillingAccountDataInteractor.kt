package ru.wb.perevozka.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.perevozka.network.monitor.NetworkState

interface CourierBillingAccountDataInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun saveAccount(courierBillingAccountEntity: CourierBillingAccountEntity): Completable
    fun getAccount(account: String): Single<CourierBillingAccountEntity>
}