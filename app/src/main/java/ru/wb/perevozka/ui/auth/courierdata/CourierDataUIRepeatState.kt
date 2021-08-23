package ru.wb.perevozka.ui.auth.courierdata

sealed class CourierDataUIRepeatState {
    object RepeatPasswordProgress : CourierDataUIRepeatState()
    data class RepeatPasswordTimer(val text: String, val timeStart: String) :
        CourierDataUIRepeatState()

    object RepeatPassword : CourierDataUIRepeatState()
    data class ErrorPassword(val message: String) : CourierDataUIRepeatState()
}