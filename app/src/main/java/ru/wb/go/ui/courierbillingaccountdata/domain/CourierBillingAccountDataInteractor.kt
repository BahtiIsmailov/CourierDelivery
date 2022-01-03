package ru.wb.go.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierBillingAccountDataInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun saveBillingAccounts(accountsEntity: List<CourierBillingAccountEntity>): Completable

    fun getBillingAccounts(): Single<List<CourierBillingAccountEntity>>

    fun getBank(bic: String): Maybe<BankEntity>

}