package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorNavAction {

    data class NavigateToAccountEdit(val inn: String, val account: String, val balance: Int) :
        CourierBillingAccountSelectorNavAction()

    data class NavigateToAccountCreate(val inn: String, val account: String, val balance: Int) :
        CourierBillingAccountSelectorNavAction()
}