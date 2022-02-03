package ru.wb.go.ui.courierbilling

import ru.wb.go.mvvm.model.base.BaseItem

sealed class CourierBillingState {

    object Init : CourierBillingState()

    data class ShowBilling(val items: List<BaseItem>) : CourierBillingState()

    data class Empty(val info: String) : CourierBillingState()

}
