package ru.wb.perevozka.ui.courierloader

sealed class CourierLoaderNavigationState {
    data class NavigateToCourierUserForm(val phone: String) : CourierLoaderNavigationState()
    data class NavigateToCouriersCompleteRegistration(val phone: String) : CourierLoaderNavigationState()
    object NavigateToCourierWarehouse : CourierLoaderNavigationState()
}