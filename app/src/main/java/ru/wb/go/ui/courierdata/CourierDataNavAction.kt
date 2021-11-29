package ru.wb.go.ui.courierdata

sealed class CourierDataNavAction {
    object NavigateToAgreement : CourierDataNavAction()
    data class NavigateToCouriersCompleteRegistration(val phone: String) : CourierDataNavAction()
}