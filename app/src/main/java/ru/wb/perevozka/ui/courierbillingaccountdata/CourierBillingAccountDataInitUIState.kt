package ru.wb.perevozka.ui.courierbillingaccountdata

import ru.wb.perevozka.network.api.app.entity.CourierBillingAccountEntity

sealed class CourierBillingAccountDataInitUIState {
    object Create : CourierBillingAccountDataInitUIState()
    data class Edit(val field: CourierBillingAccountEntity) :
        CourierBillingAccountDataInitUIState()
}