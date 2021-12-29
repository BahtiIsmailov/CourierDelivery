package ru.wb.go.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierBillingAccountSelectorInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun accounts(): Single<List<CourierBillingAccountEntity>>

    fun payments(amount: Int, paymentEntity: PaymentEntity): Completable

}