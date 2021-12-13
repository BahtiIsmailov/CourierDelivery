package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataNavAction {
    data class NavigateToAccountSelector(val inn: String, val balance: Int) :
        CourierBillingAccountDataNavAction()
}