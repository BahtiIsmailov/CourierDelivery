package ru.wb.perevozka.ui.courierintransit

sealed class CourierIntransitUIState {

    data class InitItems(
        val items: MutableList<CourierIntransitItem>
    ) :
        CourierIntransitUIState()

    data class UpdateItems(val items: MutableList<CourierIntransitItem>) : CourierIntransitUIState()

    object Empty : CourierIntransitUIState()

}