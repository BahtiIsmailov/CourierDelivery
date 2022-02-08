package ru.wb.go.ui.courierbilling

sealed class CourierBillingNavigationState {

    data class NavigateToAccountSelector(
        val balance: Int
    ) : CourierBillingNavigationState()

}
