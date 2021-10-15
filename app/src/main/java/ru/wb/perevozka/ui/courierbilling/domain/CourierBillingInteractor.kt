package ru.wb.perevozka.ui.courierbilling.domain

import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.BillingCommonEntity

interface CourierBillingInteractor {

    fun billing(): Single<BillingCommonEntity>

}