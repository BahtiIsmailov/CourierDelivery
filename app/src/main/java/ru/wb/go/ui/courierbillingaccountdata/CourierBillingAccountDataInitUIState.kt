package ru.wb.go.ui.courierbillingaccountdata

import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity

sealed class CourierBillingAccountDataInitUIState {
    object Create : CourierBillingAccountDataInitUIState()
    data class Edit(val field: CourierBillingAccountEntity) :
        CourierBillingAccountDataInitUIState()
}