package ru.wb.go.ui.courierbilling

sealed class CourierBillingNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierBillingNavigationState()

    object NavigateToBack : CourierBillingNavigationState()

    data class NavigateToAccountSelector(val inn: String, val balance: Int) :
        CourierBillingNavigationState()

    data class NavigateToAccountCreate(val inn: String, val account: String, val balance: Int) :
        CourierBillingNavigationState()

}
