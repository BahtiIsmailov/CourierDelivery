package ru.wb.go.ui.courierloader

sealed class CourierLoaderNavigationState {
    data class NavigateToCourierUserForm(val phone: String) : CourierLoaderNavigationState()
    data class NavigateToCouriersCompleteRegistration(val phone: String) :
        CourierLoaderNavigationState()

    object NavigateToCourierWarehouse : CourierLoaderNavigationState()
    object NavigateToTimer : CourierLoaderNavigationState()
    object NavigateToScanner : CourierLoaderNavigationState()
    object NavigateToIntransit : CourierLoaderNavigationState()
    object NavigateToAppUpdate : CourierLoaderNavigationState()
    object NavigateToPhone : CourierLoaderNavigationState()
    object NavigateToAgreement : CourierLoaderNavigationState()
}