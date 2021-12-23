package ru.wb.go.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorUIAction {

    data class FocusChange(
        val text: String,
        val type: CourierBillingAccountSelectorQueryType,
        val hasFocus: Boolean
    ) :
        CourierBillingAccountSelectorUIAction()

    data class TextChange(val text: String, val type: CourierBillingAccountSelectorQueryType) : CourierBillingAccountSelectorUIAction()

    data class CompleteClick(var userData: MutableList<CourierData>) : CourierBillingAccountSelectorUIAction()

}