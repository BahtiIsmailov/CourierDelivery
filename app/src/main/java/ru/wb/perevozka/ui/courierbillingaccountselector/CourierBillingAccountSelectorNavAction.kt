package ru.wb.perevozka.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorNavAction {

    data class NavigateToAccountEdit(val account: String, val balance: Int) :
        CourierBillingAccountSelectorNavAction()

    data class NavigateToAccountCreate(val account: String, val balance: Int) :
        CourierBillingAccountSelectorNavAction()
}