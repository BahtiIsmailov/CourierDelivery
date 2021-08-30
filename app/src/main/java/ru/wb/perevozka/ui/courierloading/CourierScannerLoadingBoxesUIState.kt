package ru.wb.perevozka.ui.courierloading

sealed class CourierScannerLoadingBoxesUIState {

    data class ReceptionBoxesItem(val items: MutableList<CourierScannerLoadingBoxesItem>) :
        CourierScannerLoadingBoxesUIState()

    data class ReceptionBoxItem(val index: Int, val item: CourierScannerLoadingBoxesItem) :
        CourierScannerLoadingBoxesUIState()

    object Empty : CourierScannerLoadingBoxesUIState()

    object Progress : CourierScannerLoadingBoxesUIState()

    object ProgressComplete : CourierScannerLoadingBoxesUIState()

}