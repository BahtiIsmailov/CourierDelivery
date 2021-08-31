package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingBoxesUIState {

    data class ReceptionBoxesItem(val items: MutableList<CourierLoadingBoxesItem>) :
        CourierLoadingBoxesUIState()

    data class ReceptionBoxItem(val index: Int, val item: CourierLoadingBoxesItem) :
        CourierLoadingBoxesUIState()

    object Empty : CourierLoadingBoxesUIState()

    object Progress : CourierLoadingBoxesUIState()

    object ProgressComplete : CourierLoadingBoxesUIState()

}