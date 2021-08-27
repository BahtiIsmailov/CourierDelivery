package ru.wb.perevozka.ui.courierdata

sealed class CourierDataNavAction {
    data class NavigateToCouriersCompleteRegistration(val phone: String) : CourierDataNavAction()
}