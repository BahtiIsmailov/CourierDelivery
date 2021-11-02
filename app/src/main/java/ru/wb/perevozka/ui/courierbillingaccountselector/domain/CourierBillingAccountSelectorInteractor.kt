package ru.wb.perevozka.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.perevozka.network.api.app.entity.PaymentEntity
import ru.wb.perevozka.network.monitor.NetworkState

interface CourierBillingAccountSelectorInteractor {
    fun observeNetworkConnected(): Observable<NetworkState>
    fun courierDocuments(courierDocumentsEntity: CourierBillingAccountEntity): Completable
    fun accounts(): Single <List<CourierBillingAccountEntity>>
    fun payments(paymentEntity: PaymentEntity): Completable
}