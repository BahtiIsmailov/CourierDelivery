package ru.wb.go.ui.courierbilling.domain

import io.reactivex.Single
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.api.app.entity.accounts.AccountsEntity

interface CourierBillingInteractor {

    fun billing(): Single<BillingCommonEntity>

//    fun accountsLocal(): Completable

    fun accountsRemote(): Single<AccountsEntity>

}