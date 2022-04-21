package ru.wb.go.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.ui.BaseServiceInteractor

interface CourierBillingAccountDataInteractor : BaseServiceInteractor {

    fun saveBillingAccounts(accountsEntity: List<CourierBillingAccountEntity>): Completable

    fun getBillingAccounts(): Single<List<CourierBillingAccountEntity>>

    fun getBank(bic: String): Maybe<BankEntity>

}