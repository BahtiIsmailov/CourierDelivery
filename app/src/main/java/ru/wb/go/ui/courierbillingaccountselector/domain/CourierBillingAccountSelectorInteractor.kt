package ru.wb.go.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.ui.BaseServiceInteractor

interface CourierBillingAccountSelectorInteractor: BaseServiceInteractor {

    fun payments(amount: Int, paymentEntity: PaymentEntity): Completable

    fun getBillingAccounts(): Single<List<CourierBillingAccountEntity>>

}