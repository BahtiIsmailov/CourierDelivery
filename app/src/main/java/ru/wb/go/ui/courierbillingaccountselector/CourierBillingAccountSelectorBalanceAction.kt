package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorBalanceAction {

    data class Init(val text: String) :
        CourierBillingAccountSelectorBalanceAction()

    data class Complete(val text: String) :
        CourierBillingAccountSelectorBalanceAction()

    data class Error(val text: String) :
        CourierBillingAccountSelectorBalanceAction()

}