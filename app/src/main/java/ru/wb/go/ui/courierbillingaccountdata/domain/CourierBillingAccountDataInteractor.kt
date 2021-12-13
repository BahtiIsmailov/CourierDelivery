package ru.wb.go.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierBillingAccountDataInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    //fun saveAccount(courierBillingAccountEntity: CourierBillingAccountEntity): Completable

    fun saveAccountRemote(accountEntity: CourierBillingAccountEntity): Completable

//    fun getAccount(account: String): Single<CourierBillingAccountEntity>

    //fun accountsRemote(): Single<AccountsEntity>

    fun getBank(bic: String): Maybe<BankEntity>

}