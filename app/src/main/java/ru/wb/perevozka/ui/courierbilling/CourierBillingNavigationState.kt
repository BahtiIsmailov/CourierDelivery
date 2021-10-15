package ru.wb.perevozka.ui.courierbilling

sealed class CourierBillingNavigationState {

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierBillingNavigationState()

    object NavigateToBack : CourierBillingNavigationState()

}
