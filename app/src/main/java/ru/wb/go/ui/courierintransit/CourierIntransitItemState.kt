package ru.wb.go.ui.courierintransit

import ru.wb.go.ui.courierintransit.delegates.items.BaseIntransitItem

sealed class CourierIntransitItemState {

    data class InitItems(
        val items: MutableList<BaseIntransitItem>, val boxTotal: String
    ) :
        CourierIntransitItemState()

    data class UpdateItems(val items: MutableList<BaseIntransitItem>) :
        CourierIntransitItemState()

    data class ScrollTo(val position: Int) :
        CourierIntransitItemState()

    object Empty : CourierIntransitItemState()

    object CompleteDelivery : CourierIntransitItemState()

}