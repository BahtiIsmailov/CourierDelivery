package ru.wb.perevozka.ui.splash

sealed class LoaderUINavState {
    object NavigateToApp : LoaderUINavState()
    object NavigateToCourier : LoaderUINavState()
    object NavigateToNumberPhone : LoaderUINavState()
}