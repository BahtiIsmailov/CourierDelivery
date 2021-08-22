package ru.wb.perevozka.ui.auth

sealed class AuthLoaderUINavState {
    object NavigateToNumberPhone : AuthLoaderUINavState()
    data class NavigateToUserForm(val phone: String) : AuthLoaderUINavState()
    data class NavigateToCouriersCompleteRegistration(val phone: String) : AuthLoaderUINavState()
}