package ru.wb.go.ui.courierloader

sealed class CourierLoaderUIState {

    object Progress : CourierLoaderUIState()
    object Complete : CourierLoaderUIState()
    data class Error(val message: String) : CourierLoaderUIState()

}