package ru.wb.go.ui.courierbilling

import kotlinx.parcelize.RawValue
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity

sealed class CourierBillingNavigationState {

    data class NavigateToAccountSelector(
        val balance: Int,
        val accounts: @RawValue List<CourierBillingAccountEntity>?
    ) : CourierBillingNavigationState()

    data class NavigateToAccountCreate(
        val account: CourierBillingAccountEntity?,
        val billingAccount: List<CourierBillingAccountEntity>,
        val balance: Int,
    ) :
        CourierBillingNavigationState()

}
