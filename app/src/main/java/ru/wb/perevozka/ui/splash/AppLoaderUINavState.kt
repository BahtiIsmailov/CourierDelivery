package ru.wb.perevozka.ui.splash

sealed class AppLoaderUINavState {
    object NavigateToApp : AppLoaderUINavState()
    object NavigateToCourier : AppLoaderUINavState()
    object NavigateToAuth : AppLoaderUINavState()
}