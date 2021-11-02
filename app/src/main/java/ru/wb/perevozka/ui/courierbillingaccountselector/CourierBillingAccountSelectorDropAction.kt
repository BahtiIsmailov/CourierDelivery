package ru.wb.perevozka.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorDropAction {

    data class SetItems(val items: List<CourierBillingAccountSelectorAdapterItem>) : CourierBillingAccountSelectorDropAction()

    data class SetSelected( val id: Int) : CourierBillingAccountSelectorDropAction()
}