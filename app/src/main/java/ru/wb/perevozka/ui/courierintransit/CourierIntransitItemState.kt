package ru.wb.perevozka.ui.courierintransit

import ru.wb.perevozka.ui.courierintransit.delegates.items.BaseIntransitItem

sealed class CourierIntransitItemState {

    data class InitItems(
        val items: MutableList<BaseIntransitItem>, val boxTotal: String
    ) :
        CourierIntransitItemState()

    data class UpdateItems(val items: MutableList<BaseIntransitItem>, val position: Int) :
        CourierIntransitItemState()

    object Empty : CourierIntransitItemState()

    object CompleteDelivery : CourierIntransitItemState()

}