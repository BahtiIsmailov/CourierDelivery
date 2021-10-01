package ru.wb.perevozka.ui.courierloader

sealed class CourierLoaderUIState {

    object Progress : CourierLoaderUIState()
    data class Error(val message: String) : CourierLoaderUIState()

}