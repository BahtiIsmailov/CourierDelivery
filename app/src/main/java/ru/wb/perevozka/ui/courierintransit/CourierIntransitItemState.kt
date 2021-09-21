package ru.wb.perevozka.ui.courierintransit

sealed class CourierIntransitItemState {

    data class InitItems(
        val items: MutableList<CourierIntransitItem>, val boxTotal: String
    ) :
        CourierIntransitItemState()

    data class UpdateItems(val items: MutableList<CourierIntransitItem>, val position: Int) :
        CourierIntransitItemState()

    object Empty : CourierIntransitItemState()

}