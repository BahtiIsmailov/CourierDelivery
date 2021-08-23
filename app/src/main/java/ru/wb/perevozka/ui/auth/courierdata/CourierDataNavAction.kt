package ru.wb.perevozka.ui.auth.courierdata

sealed class CourierDataNavAction {
    data class NavigateToCouriersCompleteRegistration(val phone: String) : CourierDataNavAction()
}