package ru.wb.perevozka.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataNavAction {
    data class  NavigateToAccountSelector(val balance: Int) : CourierBillingAccountDataNavAction()
}