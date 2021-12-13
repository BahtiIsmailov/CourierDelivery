package ru.wb.go.ui.courierbillingaccountdata

import ru.wb.go.network.api.app.entity.CourierBillingAccountEditableEntity

sealed class CourierBillingAccountDataInitUIState {
    object Create : CourierBillingAccountDataInitUIState()
    data class Edit(val field: CourierBillingAccountEditableEntity) :
        CourierBillingAccountDataInitUIState()
}