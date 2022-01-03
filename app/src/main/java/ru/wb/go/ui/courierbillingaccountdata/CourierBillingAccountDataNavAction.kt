package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataNavAction {

    data class NavigateToAccountSelector(val balance: Int) : CourierBillingAccountDataNavAction()
    object NavigateToBack : CourierBillingAccountDataNavAction()
    data class NavigateToConfirmDialog(val account:String) : CourierBillingAccountDataNavAction()


}