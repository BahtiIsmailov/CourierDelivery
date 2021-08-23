package ru.wb.perevozka.ui.auth.courierdata

sealed class CourierDataUIState {
    data class Error(val message: String, val type: CourierDataQueryType) : CourierDataUIState()
    data class ErrorFocus(val message: String, val type: CourierDataQueryType) : CourierDataUIState()
    data class Complete(val format: String, val type: CourierDataQueryType) : CourierDataUIState()
    object Next : CourierDataUIState()
}