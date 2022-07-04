package ru.wb.go.ui.courierbillingaccountselector

import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity

sealed class CourierBillingAccountSelectorNavAction {

    data class NavigateToAccountEdit(
        val account: CourierBillingAccountEntity,
        val billingAccounts: List<CourierBillingAccountEntity>,
        val balance: Int
    ) :
        CourierBillingAccountSelectorNavAction()

    data class NavigateToAccountCreate(
        //val account: CourierBillingAccountEntity,
        val billingAccounts: List<CourierBillingAccountEntity>,
        val balance: Int
    ) :
        CourierBillingAccountSelectorNavAction()

    data class NavigateToBillingComplete(val balance: Int) :
        CourierBillingAccountSelectorNavAction()
}