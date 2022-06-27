package ru.wb.go.ui.courierbillingaccountdata

sealed class CourierBillingAccountDataUIAction {

    data class FocusChange(
        val text: String,
        val type: CourierBillingAccountDataQueryType,
        val hasFocus: Boolean
    ) :
        CourierBillingAccountDataUIAction()

    data class TextChange(val text: String, val type: CourierBillingAccountDataQueryType) :
        CourierBillingAccountDataUIAction()

    data class SaveClick(var userData: MutableList<CourierAccountData>) :
        CourierBillingAccountDataUIAction()

}