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

    fun saveAccountRemote(accountEntity: CourierBillingAccountEntity, oldAccount: String): Completable

    fun getEditableResult(account: String): Single<EditableResult>

    fun getBank(bic: String): Maybe<BankEntity>

    fun removeAccount(account: String): Completable

}