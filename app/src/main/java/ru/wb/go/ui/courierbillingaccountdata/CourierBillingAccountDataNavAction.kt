package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataNavAction {
    data class  NavigateToAccountSelector(val balance: Int) : CourierBillingAccountDataNavAction()
}