package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingBoxesUIState {

    data class ReceptionBoxesItem(val items: MutableList<CourierUnloadingBoxesItem>) :
        CourierUnloadingBoxesUIState()

    data class ReceptionBoxItem(val index: Int, val item: CourierUnloadingBoxesItem) :
        CourierUnloadingBoxesUIState()

    object Empty : CourierUnloadingBoxesUIState()

    object Progress : CourierUnloadingBoxesUIState()

    object ProgressComplete : CourierUnloadingBoxesUIState()

}