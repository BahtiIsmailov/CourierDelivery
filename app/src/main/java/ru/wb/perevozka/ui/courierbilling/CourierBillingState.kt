package ru.wb.perevozka.ui.courierbilling

import ru.wb.perevozka.mvvm.model.base.BaseItem

sealed class CourierBillingState {

    data class ShowBilling(val items: List<BaseItem>) : CourierBillingState()

    data class Empty(val info: String) : CourierBillingState()

}
