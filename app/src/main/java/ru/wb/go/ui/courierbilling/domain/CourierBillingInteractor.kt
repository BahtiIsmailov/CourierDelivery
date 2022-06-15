package ru.wb.go.ui.courierbilling.domain

import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.ui.BaseServiceInteractor

interface CourierBillingInteractor: BaseServiceInteractor {

    suspend fun getBillingInfo():  BillingCommonEntity

}