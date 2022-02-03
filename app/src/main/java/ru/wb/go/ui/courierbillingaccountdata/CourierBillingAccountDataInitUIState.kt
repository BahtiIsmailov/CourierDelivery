package ru.wb.go.ui.courierbillingaccountdata

import ru.wb.go.network.api.app.entity.CourierBillingAccountEditableEntity

sealed class CourierBillingAccountDataInitUIState {
    data class Create(
        val userName: String,
        val userInn: String
    ) : CourierBillingAccountDataInitUIState()

    data class Edit(val field: CourierBillingAccountEditableEntity) :
        CourierBillingAccountDataInitUIState()
}