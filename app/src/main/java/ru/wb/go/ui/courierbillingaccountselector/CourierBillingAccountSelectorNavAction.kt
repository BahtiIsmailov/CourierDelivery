package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorNavAction {

    data class NavigateToAccountEdit(val account: String, val balance: Int) :
        CourierBillingAccountSelectorNavAction()

    data class NavigateToAccountCreate(val account: String, val balance: Int) :
        CourierBillingAccountSelectorNavAction()

    data class NavigateToBillingComplete(val amount: Int) :
            CourierBillingAccountSelectorNavAction()
}