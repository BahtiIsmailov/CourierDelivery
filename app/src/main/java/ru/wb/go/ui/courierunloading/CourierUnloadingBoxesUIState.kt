package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingBoxesUIState {


    object Empty : CourierUnloadingBoxesUIState()

    object Progress : CourierUnloadingBoxesUIState()

    object ProgressComplete : CourierUnloadingBoxesUIState()

}