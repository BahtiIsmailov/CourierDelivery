package ru.wb.go.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierBillingAccountSelectorInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun courierDocuments(courierDocumentsEntity: CourierBillingAccountEntity): Completable
    fun accounts(): Single <List<AccountEntity>>
    fun payments(paymentEntity: PaymentEntity): Completable
}