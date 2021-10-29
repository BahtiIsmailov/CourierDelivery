package ru.wb.perevozka.ui.courierbillingaccountselector

sealed class CourierBillingAccountSelectorDropAction {

    data class SetItems(val accounts: List<String>) : CourierBillingAccountSelectorDropAction()

    data class SetSelected( val id: Int) : CourierBillingAccountSelectorDropAction()
}