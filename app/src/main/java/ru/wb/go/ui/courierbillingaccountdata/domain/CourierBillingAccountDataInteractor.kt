package ru.wb.go.ui.courierbillingaccountdata.domain

import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.ui.BaseServiceInteractor

interface CourierBillingAccountDataInteractor : BaseServiceInteractor {

    suspend fun saveBillingAccounts(accountsEntity: List<CourierBillingAccountEntity>)

    suspend fun getBillingAccounts():  List<CourierBillingAccountEntity>

    suspend fun getBank(bic: String): BankEntity

}